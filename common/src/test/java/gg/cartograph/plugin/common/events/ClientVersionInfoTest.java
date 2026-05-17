package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClientVersionInfoTest
{

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesAllFieldsToExpectedWireKeys() throws Exception
    {
        var info = new ClientVersionInfo("viaversion", "1.8", "1.21.4", List.of(47, 765, 766, 767, 768, 769));
        var json = mapper.readTree(mapper.writeValueAsBytes(info));

        assertEquals("viaversion", json.get("s").asText());
        assertEquals("1.8", json.get("min").asText());
        assertEquals("1.21.4", json.get("max").asText());
        assertEquals(6, json.get("p").size());
        assertEquals(47, json.get("p").get(0).asInt());
        assertEquals(769, json.get("p").get(5).asInt());
    }

    @Test
    void omitsNullFields() throws Exception
    {
        var info = new ClientVersionInfo("viaversion", null, null, null);
        var json = mapper.writeValueAsString(info);

        assertFalse(json.contains("\"min\""));
        assertFalse(json.contains("\"max\""));
        assertFalse(json.contains("\"p\""));
        assertEquals("{\"s\":\"viaversion\"}", json);
    }
}
