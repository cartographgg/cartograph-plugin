package gg.cartograph.plugin.common;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GcSamplerTest
{
    @Test void returnsDeltaSinceLastSampleAndAdvancesBaseline() {
        long[][] readings = {{5, 100}, {8, 160}, {8, 160}};
        var idx = new AtomicInteger(0);
        var sampler = new GcSampler(() -> readings[idx.get()]); // baseline = {5,100}

        idx.set(1);
        var d1 = sampler.sample();                              // {8,160} - {5,100}
        assertEquals(3, d1.count());
        assertEquals(60, d1.timeMs());

        idx.set(2);
        var d2 = sampler.sample();                              // {8,160} - {8,160}
        assertEquals(0, d2.count());
        assertEquals(0, d2.timeMs());
    }

    @Test void clampsNegativeDeltaToZeroOnCounterReset() {
        long[][] readings = {{10, 200}, {4, 50}};
        var idx = new AtomicInteger(0);
        var sampler = new GcSampler(() -> readings[idx.get()]); // baseline = {10,200}

        idx.set(1);
        var d = sampler.sample();                               // decreasing → clamp to 0
        assertEquals(0, d.count());
        assertEquals(0, d.timeMs());
    }
}
