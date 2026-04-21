package gg.cartograph.plugin.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpHasherTest
{

    @Test
    void hashIsDeterministic()
    {
        var hasher = new IpHasher("test-salt");
        var hash1  = hasher.hash("192.168.1.1");
        var hash2  = hasher.hash("192.168.1.1");

        assertEquals(hash1, hash2);
    }

    @Test
    void differentIpsProduceDifferentHashes()
    {
        var hasher = new IpHasher("test-salt");
        var hash1  = hasher.hash("192.168.1.1");
        var hash2  = hasher.hash("192.168.1.2");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void differentSaltsProduceDifferentHashes()
    {
        var hasher1 = new IpHasher("salt-a");
        var hasher2 = new IpHasher("salt-b");

        assertNotEquals(hasher1.hash("192.168.1.1"), hasher2.hash("192.168.1.1"));
    }

    @Test
    void hashIsHexString()
    {
        var hasher = new IpHasher("test-salt");
        var hash   = hasher.hash("10.0.0.1");

        assertTrue(hash.matches("^[0-9a-f]+$"));
    }

    @Test
    void nullIpReturnsNull()
    {
        var hasher = new IpHasher("test-salt");

        assertNull(hasher.hash(null));
    }
}
