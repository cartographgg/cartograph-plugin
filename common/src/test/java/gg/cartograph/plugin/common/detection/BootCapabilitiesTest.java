package gg.cartograph.plugin.common.detection;

import gg.cartograph.plugin.common.events.ClientVersionInfo;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BootCapabilitiesTest
{

    /** Discards every log message. Sufficient for the absent-classpath cases. */
    private static final CartographLogger NOOP = new CartographLogger()
    {
        @Override public void debug(String message) {}
        @Override public void info(String message) {}
        @Override public void warn(String message) {}
        @Override public void error(String message) {}
        @Override public void error(String message, Throwable cause) {}
    };

    /** A test double exposing the same getter shape as ViaVersion's ProtocolVersion. */
    record FakeVersion(int version, String name)
    {
        public int getVersion() { return version; }

        public String getName() { return name; }
    }

    @Test
    void buildClientVersionReturnsNullForEmptyCollection()
    {
        var result = BootCapabilities.buildClientVersion("viaversion", List.of(), NOOP);

        assertNull(result);
    }

    @Test
    void buildClientVersionWithSingleElementProducesMinEqualsMax()
    {
        var versions = List.of(new FakeVersion(769, "1.21.4"));

        var result = BootCapabilities.buildClientVersion("viaversion", versions, NOOP);

        assertEquals("viaversion", result.source());
        assertEquals("1.21.4", result.min());
        assertEquals("1.21.4", result.max());
        assertEquals(List.of(769), result.protocols());
    }

    @Test
    void buildClientVersionWithMultipleVersionsDerivesMinMaxFromOrder()
    {
        var versions = List.of(
                new FakeVersion(47, "1.8"),
                new FakeVersion(765, "1.20.4"),
                new FakeVersion(769, "1.21.4")
        );

        var result = BootCapabilities.buildClientVersion("viaversion", versions, NOOP);

        assertEquals("viaversion", result.source());
        assertEquals("1.8", result.min());
        assertEquals("1.21.4", result.max());
        assertEquals(List.of(47, 765, 769), result.protocols());
    }

    @Test
    void detectClientVersionReturnsNullWhenViaVersionIsNotOnClasspath()
    {
        // com.viaversion.viaversion.api.Via is not on the common test classpath,
        // so the detector must return null silently.
        var result = BootCapabilities.detectClientVersion(NOOP);

        assertNull(result);
    }

    @Test
    void detectBedrockSupportReturnsNullWhenNeitherPluginIsOnClasspath()
    {
        var result = BootCapabilities.detectBedrockSupport(NOOP);

        assertNull(result);
    }
}
