package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Per-world chunk and entity counts, reported in heartbeats for worlds that
 * cross the notability threshold.
 *
 * <p>Worlds below the threshold (see {@link #CHUNK_THRESHOLD} and
 * {@link #ENTITY_THRESHOLD}) are omitted from the heartbeat's world metrics
 * list entirely. The node-level totals on the heartbeat itself always reflect
 * every loaded world, including omitted ones.</p>
 *
 * <p>The {@code name} is the world's folder name as reported by
 * {@code World#getName()} — stable across restarts and unique within a node.</p>
 *
 * @param name           the world's folder name
 * @param chunksLoaded   loaded chunks in this world
 * @param entitiesLoaded loaded entities in this world
 */
public record WorldMetrics(
        @JsonGetter("n") String name,
        @JsonGetter("cc") Integer chunksLoaded,
        @JsonGetter("ec") Integer entitiesLoaded
)
{
    /** A world with more loaded chunks than this is reported in heartbeats. */
    public static final int CHUNK_THRESHOLD = 50;

    /** A world with more loaded entities than this is reported in heartbeats. */
    public static final int ENTITY_THRESHOLD = 100;

    /**
     * Whether a world with the given counts should be included in a heartbeat's
     * per-world metrics list.
     */
    public static boolean isNotable(int chunks, int entities)
    {
        return chunks > CHUNK_THRESHOLD || entities > ENTITY_THRESHOLD;
    }
}