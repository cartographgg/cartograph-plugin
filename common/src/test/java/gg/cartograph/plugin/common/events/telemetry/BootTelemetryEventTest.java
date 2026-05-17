package gg.cartograph.plugin.common.events.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.cartograph.plugin.common.NodeType;
import gg.cartograph.plugin.common.events.BedrockSupportInfo;
import gg.cartograph.plugin.common.events.ClientVersionInfo;
import gg.cartograph.plugin.common.events.OsInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootTelemetryEventTest
{

    private final ObjectMapper mapper = new ObjectMapper();

    private BootTelemetryEvent eventWithCapabilities(ClientVersionInfo cv, BedrockSupportInfo br)
    {
        return new BootTelemetryEvent(
                1000L,
                "Paper",
                "1.21.1-R0.1-SNAPSHOT",
                "1.21.1",
                "21.0.1",
                "Eclipse Adoptium",
                new OsInfo("Linux", "6.5.0", "amd64"),
                "1.0.0-SNAPSHOT",
                NodeType.STANDALONE,
                20,
                10,
                10,
                true,
                false,
                "A Minecraft Server",
                null,
                null,
                null,
                null,
                null,
                cv,
                br
        );
    }

    @Test
    void capabilityFieldsSerializeToCvAndBr() throws Exception
    {
        var cv = new ClientVersionInfo("viaversion", "1.8", "1.21.4", List.of(47, 769));
        var br = new BedrockSupportInfo(true, true, ".");
        var json = mapper.readTree(mapper.writeValueAsBytes(eventWithCapabilities(cv, br)));

        assertEquals("viaversion", json.get("cv").get("s").asText());
        assertEquals("1.8", json.get("cv").get("min").asText());
        assertEquals("1.21.4", json.get("cv").get("max").asText());
        assertEquals(2, json.get("cv").get("p").size());
        assertEquals(true, json.get("br").get("g").asBoolean());
        assertEquals(true, json.get("br").get("f").asBoolean());
        assertEquals(".", json.get("br").get("fp").asText());
    }

    @Test
    void capabilityFieldsOmittedWhenNull() throws Exception
    {
        var json = mapper.writeValueAsString(eventWithCapabilities(null, null));

        assertFalse(json.contains("\"cv\""));
        assertFalse(json.contains("\"br\""));
    }

    @Test
    void eventTypeKeyIsBoot() throws Exception
    {
        var json = mapper.readTree(mapper.writeValueAsBytes(eventWithCapabilities(null, null)));

        assertTrue(json.has("t"));
        assertEquals("b", json.get("t").asText());
    }
}
