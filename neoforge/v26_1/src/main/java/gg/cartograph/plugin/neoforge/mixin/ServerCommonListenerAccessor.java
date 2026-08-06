package gg.cartograph.plugin.neoforge.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the {@code protected} {@code connection} field on the common packet
 * listener so the shim can reach a player's {@link Connection} (and the handshake
 * data stashed on it) at join.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonListenerAccessor
{
    @Accessor("connection")
    Connection cartographConnection();
}
