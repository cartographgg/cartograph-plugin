package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * HTTP transport for flushed telemetry batches.
 *
 * <p>Wraps each batch in a {@link TelemetryEnvelope}, serializes to JSON,
 * gzip-compresses the payload, and POSTs it to the Cartograph ingest endpoint.
 * Designed to be called synchronously on the buffer's flush thread.</p>
 *
 * <p>Error handling:</p>
 * <ul>
 *     <li><b>2xx</b> — success, returns normally</li>
 *     <li><b>4xx</b> — logs an error, returns normally (batch discarded)</li>
 *     <li><b>5xx</b> — throws {@link RuntimeException} to trigger buffer retry</li>
 * </ul>
 */
public class TelemetryClient
{

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final String apiEndpoint;

    private final String apiKey;

    private final CartographLogger logger;

    private final HttpClient httpClient;

    private final ObjectMapper mapper;

    public TelemetryClient(String apiEndpoint, String apiKey, CartographLogger logger)
    {
        this(
                apiEndpoint,
                apiKey,
                logger,
                HttpClient.newBuilder()
                          .connectTimeout(REQUEST_TIMEOUT)
                          .build()
        );
    }

    TelemetryClient(String apiEndpoint, String apiKey, CartographLogger logger, HttpClient httpClient)
    {
        this.apiEndpoint = apiEndpoint;
        this.apiKey      = apiKey;
        this.logger      = logger;
        this.httpClient  = httpClient;
        this.mapper      = new ObjectMapper();
    }

    /**
     * Sends a batch of telemetry events to the Cartograph API.
     *
     * <p>If the API key is blank, logs a warning and returns without sending.
     * The batch is consumed (not retried) since there is no valid destination.</p>
     *
     * @param events the batch of events to send
     */
    public void send(List<TelemetryEvent> events)
    {
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Telemetry not sent — API key is not configured");
            return;
        }

        var envelope = new TelemetryEnvelope(System.currentTimeMillis(), events);

        byte[] compressed;
        try {
            var json = mapper.writeValueAsBytes(envelope);
            compressed = gzip(json);
        } catch (IOException e) {
            logger.error("Failed to serialize telemetry batch", e);
            return;
        }

        var request = HttpRequest.newBuilder()
                                 .uri(URI.create(apiEndpoint + "/ingest"))
                                 .timeout(REQUEST_TIMEOUT)
                                 .header("Authorization", "Bearer " + apiKey)
                                 .header("Content-Type", "application/json")
                                 .header("Content-Encoding", "gzip")
                                 .POST(HttpRequest.BodyPublishers.ofByteArray(compressed))
                                 .build();

        HttpResponse<Void> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("HTTP request to Cartograph API failed", e);
        }

        var status = response.statusCode();

        if (status >= 200 && status < 300) {
            logger.debug("Telemetry batch of " + events.size() + " events sent successfully");
        } else if (status >= 400 && status < 500) {
            logger.error("Cartograph API rejected batch with status " + status + " — discarding");
        } else if (status >= 500) {
            throw new RuntimeException("Cartograph API returned status " + status);
        } else {
            logger.warn("Cartograph API returned unexpected status " + status + " — discarding");
        }
    }

    /**
     * Closes the underlying {@link HttpClient}, releasing its thread pool
     * and connection pool. Should be called during plugin shutdown.
     */
    public void close()
    {
        httpClient.close();
    }

    private byte[] gzip(byte[] data) throws IOException
    {
        var baos = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }
}
