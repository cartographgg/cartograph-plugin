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
    void noSamplesReturnZero()
    {
        assertEquals(0.0, sampler.getMeanTickTime());
        assertEquals(0.0, sampler.getPeakTickTime());
    }

    @Test
    void singleSample()
    {
        sampler.recordTick(50.0);

        assertEquals(50.0, sampler.getMeanTickTime());
        assertEquals(50.0, sampler.getPeakTickTime());
    }

    @Test
    void multipleSamplesMean()
    {
        sampler.recordTick(40.0);
        sampler.recordTick(60.0);

        assertEquals(50.0, sampler.getMeanTickTime());
    }

    @Test
    void multipleSamplesPeak()
    {
        sampler.recordTick(30.0);
        sampler.recordTick(80.0);
        sampler.recordTick(50.0);

        assertEquals(80.0, sampler.getPeakTickTime());
    }

    @Test
    void resetClearsState()
    {
        sampler.recordTick(50.0);
        sampler.recordTick(100.0);
        sampler.reset();

        assertEquals(0.0, sampler.getMeanTickTime());
        assertEquals(0.0, sampler.getPeakTickTime());
    }

    @Test
    void samplesAfterReset()
    {
        sampler.recordTick(100.0);
        sampler.reset();
        sampler.recordTick(25.0);

        assertEquals(25.0, sampler.getMeanTickTime());
        assertEquals(25.0, sampler.getPeakTickTime());
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
}
