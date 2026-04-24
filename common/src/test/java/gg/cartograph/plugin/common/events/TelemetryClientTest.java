package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class TelemetryClientTest
{

    private HttpClient httpClient;

    private CartographLogger logger;

    @BeforeEach
    void setUp()
    {
        httpClient = mock(HttpClient.class);
        logger     = mock(CartographLogger.class);
    }

    private TelemetryEvent event(String type)
    {
        return new TelemetryEvent()
        {
            @Override
            public String type()
            {
                return type;
            }

            @Override
            public Long timestamp()
            {
                return 1000L;
            }
        };
    }

    @Test
    void skipsWithWarningWhenApiKeyIsBlank() throws Exception
    {
        var client = new TelemetryClient("https://api.cartograph.gg", "", logger, httpClient);

        client.send(List.of(event("h")));

        verify(logger).warn("Telemetry not sent \u2014 API key is not configured");
        verify(httpClient, never()).send(any(), any());
    }

    @Test
    void skipsWithWarningWhenApiKeyIsNull() throws Exception
    {
        var client = new TelemetryClient("https://api.cartograph.gg", null, logger, httpClient);

        client.send(List.of(event("h")));

        verify(logger).warn("Telemetry not sent \u2014 API key is not configured");
        verify(httpClient, never()).send(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void sendsGzippedPostOn200() throws Exception
    {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        var client = new TelemetryClient("https://api.cartograph.gg", "test-key", logger, httpClient);

        assertDoesNotThrow(() -> client.send(List.of(event("h"))));

        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any());

        var request = captor.getValue();
        assertEquals("https://api.cartograph.gg/ingest", request.uri().toString());
        assertEquals("Bearer test-key", request.headers().firstValue("Authorization").orElse(""));
        assertEquals("application/json", request.headers().firstValue("Content-Type").orElse(""));
        assertEquals("gzip", request.headers().firstValue("Content-Encoding").orElse(""));
        assertEquals("POST", request.method());
    }

    @SuppressWarnings("unchecked")
    @Test
    void logsErrorAndDoesNotThrowOn4xx() throws Exception
    {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(401);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        var client = new TelemetryClient("https://api.cartograph.gg", "bad-key", logger, httpClient);

        assertDoesNotThrow(() -> client.send(List.of(event("h"))));

        verify(logger).error("Cartograph API rejected batch with status 401 \u2014 discarding");
    }

    @SuppressWarnings("unchecked")
    @Test
    void throwsOnServerError() throws Exception
    {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(503);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        var client = new TelemetryClient("https://api.cartograph.gg", "test-key", logger, httpClient);

        var ex = assertThrows(RuntimeException.class, () -> client.send(List.of(event("h"))));
        assertTrue(ex.getMessage().contains("503"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void throwsOnIOException() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("connection refused"));

        var client = new TelemetryClient("https://api.cartograph.gg", "test-key", logger, httpClient);

        assertThrows(RuntimeException.class, () -> client.send(List.of(event("h"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void bodyIsGzippedJsonEnvelope() throws Exception
    {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        var client = new TelemetryClient("https://api.cartograph.gg", "test-key", logger, httpClient);
        client.send(List.of(event("h")));

        var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any());

        // Extract the body bytes from the request's BodyPublisher
        var publisher   = captor.getValue().bodyPublisher().orElseThrow();
        var flowAdapter = new Flow.Subscriber<ByteBuffer>()
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            @Override
            public void onSubscribe(Flow.Subscription subscription)
            {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item)
            {
                var bytes = new byte[item.remaining()];
                item.get(bytes);
                baos.write(bytes, 0, bytes.length);
            }

            @Override
            public void onError(Throwable throwable)
            {
            }

            @Override
            public void onComplete()
            {
            }
        };
        publisher.subscribe(flowAdapter);

        // Decompress gzip
        var gzipInput    = new GZIPInputStream(new ByteArrayInputStream(flowAdapter.baos.toByteArray()));
        var decompressed = gzipInput.readAllBytes();

        // Parse and verify envelope structure
        var mapper = new ObjectMapper();
        var json   = mapper.readTree(decompressed);

        assertEquals(1, json.get("v").asInt());
        assertTrue(json.get("a").asLong() > 0);
        assertEquals(1, json.get("e").size());
        assertEquals("h", json.get("e").get(0).get("t").asText());
    }

    @SuppressWarnings("unchecked")
    @Test
    void logsDebugBeforeSending() throws Exception
    {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        var client = new TelemetryClient("https://api.cartograph.gg", "test-key", logger, httpClient);
        client.send(List.of(event("h")));

        verify(logger).debug("Sending batch of 1 events to https://api.cartograph.gg/ingest");
    }

    @SuppressWarnings("unchecked")
    @Test
    void logsCompressedPayloadSize() throws Exception
    {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        var client = new TelemetryClient("https://api.cartograph.gg", "test-key", logger, httpClient);
        client.send(List.of(event("h")));

        verify(logger).debug(contains("Compressed payload:"));
    }
}
