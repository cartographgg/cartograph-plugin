package gg.cartograph.plugin.common;

import gg.cartograph.plugin.common.config.BufferConfig;
import gg.cartograph.plugin.common.config.FailureMode;
import gg.cartograph.plugin.common.events.spool.DiskSpool;
import gg.cartograph.plugin.common.events.spool.MemorySpool;
import gg.cartograph.plugin.common.events.spool.NoopSpool;
import gg.cartograph.plugin.common.logging.CartographLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CartographSpoolSelectionTest
{
    private final CartographLogger logger = mock(CartographLogger.class);

    private BufferConfig cfg(FailureMode mode) {
        var c = new BufferConfig();
        c.setFailureMode(mode);
        return c;
    }

    @Test void diskModeBuildsDiskSpool(@TempDir Path dir) {
        assertInstanceOf(DiskSpool.class, Cartograph.selectSpool(cfg(FailureMode.DISK), dir, logger));
    }
    @Test void memoryModeBuildsMemorySpool(@TempDir Path dir) {
        assertInstanceOf(MemorySpool.class, Cartograph.selectSpool(cfg(FailureMode.MEMORY), dir, logger));
    }
    @Test void noneModeBuildsNoopSpool(@TempDir Path dir) {
        assertInstanceOf(NoopSpool.class, Cartograph.selectSpool(cfg(FailureMode.NONE), dir, logger));
    }
    @Test void diskFailureFallsBackToMemory() {
        // A path under a file (not a dir) cannot be created → fallback.
        Path bad = Path.of("/dev/null/cartograph-spool");
        assertInstanceOf(MemorySpool.class, Cartograph.selectSpool(cfg(FailureMode.DISK), bad, logger));
    }
}
