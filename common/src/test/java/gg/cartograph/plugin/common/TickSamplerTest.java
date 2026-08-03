package gg.cartograph.plugin.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TickSamplerTest
{

    private TickSampler sampler;

    @BeforeEach
    void setUp()
    {
        sampler = new TickSampler();
    }

    @Test
    void tpsStartsAtTwenty()
    {
        var tps = sampler.getTps();

        assertEquals(3, tps.length);
        assertEquals(20.0, tps[0]);
        assertEquals(20.0, tps[1]);
        assertEquals(20.0, tps[2]);
    }

    @Test
    void tpsReflectsActualRate()
    {
        var tps = sampler.getTps();

        // Initial values before any updates fire are 20.0
        assertEquals(20.0, tps[0]);
        assertEquals(20.0, tps[1]);
        assertEquals(20.0, tps[2]);
    }

    @Test
    void percentilesOverHundredSamples()
    {
        for (int i = 1; i <= 100; i++) {
            sampler.recordTick(i);
        }
        var p = sampler.getPercentiles();

        assertEquals(50.0, p.p50());
        assertEquals(95.0, p.p95());
        assertEquals(99.0, p.p99());
        assertEquals(100.0, p.max());
    }

    @Test
    void percentilesEmptyReturnsZero()
    {
        var p = sampler.getPercentiles();
        assertEquals(0.0, p.p50());
        assertEquals(0.0, p.max());
    }

    @Test
    void percentilesResetClears()
    {
        sampler.recordTick(500.0);
        sampler.reset();
        var p = sampler.getPercentiles();
        assertEquals(0.0, p.max());
    }
}
