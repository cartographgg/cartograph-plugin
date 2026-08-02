package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PlayerJoinTelemetryEventTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    private PlayerJoinTelemetryEvent event(String hostname)
    {
        return new PlayerJoinTelemetryEvent(
                1000L,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Steve",
                true,
                null,
                "en_GB",
                "world",
                null,
                hostname
        );
    }

    @Test
    void hostnameSerializesToHn() throws Exception
    {
        var json = mapper.readTree(mapper.writeValueAsBytes(event("eu.example.com")));

        assertEquals("eu.example.com", json.get("hn").asText());
    }

    @Test
    void hostnameOmittedWhenNull() throws Exception
    {
        var json = mapper.writeValueAsString(event(null));

        assertFalse(json.contains("\"hn\""));
    }

    @Test
    void eventTypeKeyIsJoin() throws Exception
    {
        var json = mapper.readTree(mapper.writeValueAsBytes(event(null)));

        assertEquals("j", json.get("t").asText());
    }
}
