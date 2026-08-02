package gg.cartograph.plugin.bukkit;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerHostnameStashTest
{
    private final UUID player = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void takeReturnsStoredHostname()
    {
        var stash = new PlayerHostnameStash();
        stash.put(player, "eu.example.com");

        assertEquals("eu.example.com", stash.take(player));
    }

    @Test
    void takeClearsEntry()
    {
        var stash = new PlayerHostnameStash();
        stash.put(player, "eu.example.com");

        stash.take(player);

        assertNull(stash.take(player));
    }

    @Test
    void takeReturnsNullWhenAbsent()
    {
        var stash = new PlayerHostnameStash();

        assertNull(stash.take(player));
    }
}
