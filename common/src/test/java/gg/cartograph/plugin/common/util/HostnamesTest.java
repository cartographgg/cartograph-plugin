package gg.cartograph.plugin.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HostnamesTest
{
    @Test
    void nullInputReturnsNull()
    {
        assertNull(Hostnames.normalize(null));
    }

    @Test
    void blankInputReturnsNull()
    {
        assertNull(Hostnames.normalize("   "));
    }

    @Test
    void stripsForgeFmlMarker()
    {
        assertEquals("mc.example.com", Hostnames.normalize("mc.example.com\u0000FML3\u0000"));
    }

    @Test
    void stripsForwardingDataIncludingClientIp()
    {
        assertEquals(
                "mc.example.com",
                Hostnames.normalize("mc.example.com\u000012.34.56.78\u0000uuid\u0000props"));
    }

    @Test
    void stripsTrailingDot()
    {
        assertEquals("mc.example.com", Hostnames.normalize("mc.example.com."));
    }

    @Test
    void lowercases()
    {
        assertEquals("mc.example.com", Hostnames.normalize("MC.Example.COM"));
    }

    @Test
    void stripsPort()
    {
        assertEquals("mc.example.com", Hostnames.normalize("mc.example.com:25565"));
    }

    @Test
    void portOnlyInputReturnsNull()
    {
        assertNull(Hostnames.normalize(":25565"));
    }

    @Test
    void stripsBracketsAndPortFromIpv6()
    {
        assertEquals("::1", Hostnames.normalize("[::1]:25565"));
    }

    @Test
    void leavesBareIpv6LiteralIntactButLowercased()
    {
        assertEquals("fe80::1", Hostnames.normalize("FE80::1"));
    }
}
