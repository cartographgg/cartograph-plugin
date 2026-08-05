package gg.cartograph.plugin.neoforge;

import gg.cartograph.plugin.common.GcDelta;
import gg.cartograph.plugin.common.TickPercentiles;
import gg.cartograph.plugin.common.events.telemetry.HeartbeatTelemetryEvent;
import gg.cartograph.plugin.common.world.WorldStatsSnapshot;

import java.lang.management.ManagementFactory;

/**
 * Version-neutral assembler for the NeoForge heartbeat event.
 *
 * <p>Deliberately free of any {@code net.neoforged}/{@code net.minecraft}
 * reference so it can be unit-tested on a plain JDK with a fake
 * {@link NeoForgePlatform} — no server, and no NeoForge on the classpath
 * (the {@code @Mod} class cannot be loaded in a unit test).</p>
 */
final class NeoForgeHeartbeat
{

    private NeoForgeHeartbeat() {}

    static HeartbeatTelemetryEvent build(
            NeoForgePlatform platform, Object server, Integer interval,
            TickPercentiles pct, WorldStatsSnapshot stats, GcDelta gc)
    {
        var runtime = Runtime.getRuntime();
        var osBean  = (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        return new HeartbeatTelemetryEvent(
                System.currentTimeMillis(),
                new double[] {platform.averageTickTimeMs(server)},
                pct.max(),
                platform.playerCount(server),
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.maxMemory(),
                osBean.getProcessCpuLoad(),
                osBean.getCpuLoad(),
                stats.chunksLoaded(),
                stats.entitiesLoaded(),
                stats.notableWorlds().isEmpty() ? null : stats.notableWorlds(),
                pct.p50(),
                pct.p95(),
                pct.p99(),
                interval,
                gc.count(),
                gc.timeMs()
        );
    }
}
