package gg.cartograph.plugin.common;

/**
 * Platform-agnostic logging abstraction for Cartograph common components.
 *
 * <p>Each platform provides a trivial implementation wrapping its native logger:</p>
 * <ul>
 *     <li>Bukkit/BungeeCord — {@code java.util.logging.Logger}</li>
 *     <li>Velocity — {@code org.slf4j.Logger}</li>
 *     <li>NeoForge — {@code org.apache.logging.log4j.Logger}</li>
 * </ul>
 */
public interface CartographLogger
{
    void info(String message);

    void warn(String message);

    void error(String message);

    void error(String message, Throwable cause);
}
