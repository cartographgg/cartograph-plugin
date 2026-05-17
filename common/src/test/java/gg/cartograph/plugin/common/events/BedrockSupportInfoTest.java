package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BedrockSupportInfoTest
{

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesAllFieldsToExpectedWireKeys() throws Exception
    {
        var info = new BedrockSupportInfo(true, true, ".");
        var json = mapper.readTree(mapper.writeValueAsBytes(info));

        assertEquals(true, json.get("g").asBoolean());
        assertEquals(true, json.get("f").asBoolean());
        assertEquals(".", json.get("fp").asText());
    }

    @Test
    void omitsNullFields() throws Exception
    {
        var info = new BedrockSupportInfo(true, null, null);
        var json = mapper.writeValueAsString(info);

        assertEquals("{\"g\":true}", json);
        assertFalse(json.contains("\"f\""));
        assertFalse(json.contains("\"fp\""));
    }

    @Test
    void floodgatePresentWithoutGeyser() throws Exception
    {
        var info = new BedrockSupportInfo(null, true, ".");
        var json = mapper.writeValueAsString(info);

        assertEquals("{\"f\":true,\"fp\":\".\"}", json);
        assertFalse(json.contains("\"g\""));
    }
}
