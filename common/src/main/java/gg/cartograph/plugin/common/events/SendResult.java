package gg.cartograph.plugin.common.events;

import java.time.Duration;

/** Outcome of one send attempt. {@code retryAfter} is non-null only for RETRY. */
public record SendResult(Kind kind, Duration retryAfter)
{
    public enum Kind { OK, RETRY, DISCARD }

    public static SendResult ok() { return new SendResult(Kind.OK, null); }
    public static SendResult discard() { return new SendResult(Kind.DISCARD, null); }
    public static SendResult retry(Duration retryAfter) { return new SendResult(Kind.RETRY, retryAfter); }

    public boolean isOk() { return kind == Kind.OK; }
    public boolean isRetry() { return kind == Kind.RETRY; }
    public boolean isDiscard() { return kind == Kind.DISCARD; }
}
