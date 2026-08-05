package gg.cartograph.plugin.common.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FailureModeTest
{
    @Test void parsesCaseInsensitively() {
        assertEquals(FailureMode.MEMORY, FailureMode.from("memory"));
        assertEquals(FailureMode.NONE, FailureMode.from("NONE"));
        assertEquals(FailureMode.DISK, FailureMode.from("Disk"));
    }
    @Test void unknownAndNullDefaultToDisk() {
        assertEquals(FailureMode.DISK, FailureMode.from("nonsense"));
        assertEquals(FailureMode.DISK, FailureMode.from(null));
    }
}
