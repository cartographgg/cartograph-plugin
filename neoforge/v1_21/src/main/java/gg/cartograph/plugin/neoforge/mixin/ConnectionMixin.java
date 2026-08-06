package gg.cartograph.plugin.neoforge.mixin;

import gg.cartograph.plugin.neoforge.CartographHandshakeInfo;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stashes the handshake-captured protocol + hostname on the {@link Connection}, the
 * one object that persists from handshake through to the player at join. Fields are
 * {@code volatile} because the handshake runs on the Netty thread and the read runs
 * on the server thread.
 */
@Mixin(Connection.class)
public abstract class ConnectionMixin implements CartographHandshakeInfo
{
    @Unique
    private volatile Integer cartograph$protocol;

    @Unique
    private volatile String cartograph$hostname;

    @Override
    public Integer cartographProtocol()
    {
        return cartograph$protocol;
    }

    @Override
    public String cartographHostname()
    {
        return cartograph$hostname;
    }

    @Override
    public void cartographSetHandshake(Integer protocol, String hostname)
    {
        this.cartograph$protocol = protocol;
        this.cartograph$hostname = hostname;
    }
}
