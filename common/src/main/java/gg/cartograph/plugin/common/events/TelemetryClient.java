package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.logging.CartographLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

/**
 * HTTP transport for flushed telemetry batches.
 *
 * <p>Gzip-compresses a pre-serialized payload and POSTs it to the Cartograph
 * ingest endpoint. Designed to be called synchronously on the buffer's flush
 * thread — {@link #send(byte[])} never throws, it reports the outcome via a
 * {@link SendResult} instead.</p>
 *
 * <p>Response handling:</p>
 * <ul>
 *     <li><b>2xx</b> — {@link SendResult#ok()}</li>
 *     <li><b>429 / 503</b> — {@link SendResult#retry(Duration)}, honoring the
 *         {@code Retry-After} header when present</li>
 *     <li><b>other 5xx, or an I/O failure</b> — {@link SendResult#retry(Duration)}
 *         with no {@code Retry-After} hint</li>
 *     <li><b>other 4xx</b> — {@link SendResult#discard()} (batch is not retried)</li>
 * </ul>
 */
public class TelemetryClient
{

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final String apiEndpoint;

    private final String apiKey;

    private final CartographLogger logger;

    private final HttpClient httpClient;

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
    }

    /**
     * Sends a batch of telemetry (pre-serialized bytes) to the Cartograph API.
     *
     * <p>Gzip-compresses the payload and POSTs to the ingest endpoint, returning
     * a {@link SendResult} that indicates success, retry, or discard. Honors the
     * HTTP {@code Retry-After} header for 429 and 503 responses.</p>
     *
     * @param payload the pre-serialized telemetry batch (typically JSON)
     * @return a SendResult indicating success, retry, or discard
     */
    public SendResult send(byte[] payload)
    {
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Telemetry not sent — API key is not configured");
            return SendResult.discard();
        }

        byte[] compressed;
        try {
            compressed = gzip(payload);
        } catch (IOException e) {
            logger.error("Failed to compress telemetry batch", e);
            return SendResult.discard();
        }
        logger.debug("Sending batch to " + apiEndpoint + "/ingest (" + compressed.length + " bytes)");

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
        } catch (IOException e) {
            String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            logger.warn("Cartograph API request failed (" + reason + ") — will retry");
            return SendResult.retry(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SendResult.retry(null);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return SendResult.ok();
        } else if (status == 429 || status == 503) {
            return SendResult.retry(parseRetryAfter(response).orElse(null));
        } else if (status >= 500) {
            return SendResult.retry(null);
        } else {
            logger.error("Cartograph API rejected batch with status " + status + " — discarding");
            return SendResult.discard();
        }
    }

    private Optional<Duration> parseRetryAfter(HttpResponse<?> response)
    {
        var header = response.headers().firstValue("Retry-After");
        if (header.isEmpty()) {
            return Optional.empty();
        }
        String raw = header.get().trim();
        try {
            return Optional.of(Duration.ofSeconds(Math.max(0, Long.parseLong(raw))));
        } catch (NumberFormatException ignored) {
            try {
                var when = java.time.ZonedDateTime.parse(raw, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);
                long secs = java.time.Duration.between(java.time.ZonedDateTime.now(), when).toSeconds();
                return Optional.of(Duration.ofSeconds(Math.max(0, secs)));
            } catch (Exception e) {
                return Optional.empty();
            }
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
