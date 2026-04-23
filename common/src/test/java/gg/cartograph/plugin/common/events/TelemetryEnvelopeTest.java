package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryEnvelopeTest
{

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesToExpectedJsonStructure() throws Exception
    {
        var event = new TelemetryEvent()
        {
            @Override
            public String type()
            {
                return "h";
            }

            @Override
            public Long timestamp()
            {
                return 1000L;
            }
        };

        var envelope = new TelemetryEnvelope(1745329800000L, List.of(event));
        var json     = mapper.readTree(mapper.writeValueAsBytes(envelope));

        assertEquals(1, json.get("v").asInt());
        assertEquals(1745329800000L, json.get("a").asLong());
        assertEquals(1, json.get("e").size());
        assertEquals("h", json.get("e").get(0).get("t").asText());
        assertEquals(1000, json.get("e").get(0).get("ts").asLong());
    }

    @Test
    void schemaVersionIsAlwaysOne()
    {
        var envelope = new TelemetryEnvelope(0L, List.of());

        assertEquals(1, envelope.schemaVersion());
    }
}
