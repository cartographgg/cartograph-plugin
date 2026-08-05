package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TelemetryPayloadFactoryTest
{
    private TelemetryEvent event(String type) {
        return new TelemetryEvent() {
            public String type() { return type; }
            public Long timestamp() { return 1000L; }
        };
    }

    @Test void stampsSentAtEqualToCreatedAtAndSerializesEnvelope() throws Exception {
        var factory = new TelemetryPayloadFactory(() -> 4242L);
        var batch = factory.prepare(List.of(event("h")));

        assertEquals(4242L, batch.createdAtEpochMs());
        var json = new ObjectMapper().readTree(batch.payload());
        assertEquals(1, json.get("v").asInt());
        assertEquals(4242L, json.get("a").asLong());
        assertEquals(1, json.get("e").size());
        assertEquals("h", json.get("e").get(0).get("t").asText());
    }
}
