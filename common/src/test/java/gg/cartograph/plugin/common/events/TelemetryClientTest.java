package gg.cartograph.plugin.common.events;

import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private byte[] payload() { return "{\"v\":1}".getBytes(); }

    @SuppressWarnings("unchecked")
    private HttpResponse<Void> response(int status, Map<String, List<String>> headers) {
        var r = (HttpResponse<Void>) mock(HttpResponse.class);
        when(r.statusCode()).thenReturn(status);
        when(r.headers()).thenReturn(HttpHeaders.of(headers, (a, b) -> true));
        return r;
    }

    @Test void byteSendBlankKeyDiscards() throws Exception {
        var client = new TelemetryClient("https://api.cartograph.gg", "", logger, httpClient);
        assertTrue(client.send(payload()).isDiscard());
        verify(httpClient, never()).send(any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test void byteSendOk200() throws Exception {
        var resp = response(200, Map.of());
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(resp);
        var client = new TelemetryClient("https://api.cartograph.gg", "k", logger, httpClient);
        assertTrue(client.send(payload()).isOk());
    }

    @SuppressWarnings("unchecked")
    @Test void byteSend4xxDiscards() throws Exception {
        var resp = response(401, Map.of());
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(resp);
        var client = new TelemetryClient("https://api.cartograph.gg", "bad", logger, httpClient);
        assertTrue(client.send(payload()).isDiscard());
        verify(logger).error("Cartograph API rejected batch with status 401 — discarding");
    }

    @SuppressWarnings("unchecked")
    @Test void byteSend5xxRetriesWithoutRetryAfter() throws Exception {
        var resp = response(503, Map.of());
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(resp);
        var client = new TelemetryClient("https://api.cartograph.gg", "k", logger, httpClient);
        var r = client.send(payload());
        assertTrue(r.isRetry());
        assertNull(r.retryAfter());
    }

    @SuppressWarnings("unchecked")
    @Test void byteSendRetryAfterSecondsHonored() throws Exception {
        var resp = response(429, Map.of("Retry-After", List.of("120")));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(resp);
        var client = new TelemetryClient("https://api.cartograph.gg", "k", logger, httpClient);
        var r = client.send(payload());
        assertTrue(r.isRetry());
        assertEquals(Duration.ofSeconds(120), r.retryAfter());
    }

    @SuppressWarnings("unchecked")
    @Test void byteSendIoExceptionRetries() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("refused"));
        var client = new TelemetryClient("https://api.cartograph.gg", "k", logger, httpClient);
        assertTrue(client.send(payload()).isRetry());
    }
}
