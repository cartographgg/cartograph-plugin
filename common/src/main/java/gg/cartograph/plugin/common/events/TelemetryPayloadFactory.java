package gg.cartograph.plugin.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.cartograph.plugin.common.events.spool.PreparedBatch;
import gg.cartograph.plugin.common.events.telemetry.TelemetryEvent;

import java.io.IOException;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Builds a {@link TelemetryEnvelope} from a batch and serializes it to uncompressed
 * JSON. {@code sent_at} is stamped at prepare time, so a spilled batch replayed later
 * keeps its original time.
 */
public class TelemetryPayloadFactory
{
    private final ObjectMapper mapper = new ObjectMapper();
    private final LongSupplier clock;

    public TelemetryPayloadFactory() { this(System::currentTimeMillis); }

    TelemetryPayloadFactory(LongSupplier clock) { this.clock = clock; }

    public PreparedBatch prepare(List<TelemetryEvent> events) throws IOException
    {
        long now = clock.getAsLong();
        byte[] json = mapper.writeValueAsBytes(new TelemetryEnvelope(now, events));
        return new PreparedBatch(json, now);
    }
}
