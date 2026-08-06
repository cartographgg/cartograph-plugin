package gg.cartograph.plugin.neoforge.mixin;

import gg.cartograph.plugin.neoforge.CartographHandshakeInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the client protocol + host from the handshake and stashes them on the
 * {@link Connection} for the join listener to read via {@link CartographHandshakeInfo}.
 */
@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakeMixin
{
    @Shadow
    @Final
    private Connection connection;

    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void cartographCaptureHandshake(ClientIntentionPacket packet, CallbackInfo ci)
    {
        if (this.connection instanceof CartographHandshakeInfo info) {
            info.cartographSetHandshake(packet.protocolVersion(), packet.hostName());
        }
    }
}
