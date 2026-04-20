package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseWatcherAccess;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin
        extends ServerCommonPacketListenerImpl
        implements ServerGamePacketListener,
        ServerPlayerConnection,
        TickablePacketListener,
        GameProtocols.Context,
        HypoverseWatcherAccess {
    private ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    @Unique
    private final HypoverseWatcher hypoverseWatcher = new HypoverseWatcher();

    @Override
    public HypoverseWatcher voidstain_hypoidol$getHypoverseWatcher() {
        return this.hypoverseWatcher;
    }
}
