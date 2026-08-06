package gg.cartograph.plugin.neoforge;

/**
 * Duck-interface mixed into {@code net.minecraft.network.Connection} to carry the
 * client protocol version and connection hostname captured at the handshake (they
 * are not retained on the play-phase connection). {@code net.minecraft}-free so it
 * lives in the parent module and is shared by both version modules' mixins.
 */
public interface CartographHandshakeInfo
{
    /** The client's declared protocol version, or {@code null} if not captured. */
    Integer cartographProtocol();

    /** The raw handshake host (may carry an FML marker), or {@code null}. */
    String cartographHostname();

    void cartographSetHandshake(Integer protocol, String hostname);
}
