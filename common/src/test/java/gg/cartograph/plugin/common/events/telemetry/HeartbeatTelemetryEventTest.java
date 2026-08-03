package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeartbeatTelemetryEventTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void percentileAndIntervalKeysSerialize() throws Exception
    {
        var event = new HeartbeatTelemetryEvent(
                1000L, new double[]{20.0, 20.0, 20.0}, 95.0,
                5, 100L, 200L, 0.1, 0.2, 500, 1000, null,
                48.0, 80.0, 92.0, 60
        );
        var json = mapper.readTree(mapper.writeValueAsBytes(event));

        assertEquals(48.0, json.get("p50").asDouble());
        assertEquals(80.0, json.get("p95").asDouble());
        assertEquals(92.0, json.get("p99").asDouble());
        assertEquals(95.0, json.get("pt").asDouble());
        assertEquals(60, json.get("iv").asInt());
    }

    @Test
    void droppedKeysAreAbsent() throws Exception
    {
        var event = new HeartbeatTelemetryEvent(
                1000L, null, null, 5, 100L, 200L, 0.1, 0.2, null, null, null,
                null, null, null, 60
        );
        var json = mapper.writeValueAsString(event);

        assertFalse(json.contains("\"mt\""));
        assertFalse(json.contains("\"th\""));
    }
}
