package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseWatcherAccess;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

import java.util.Set;

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
    private final HypoverseWatcher hypoverseWatcher = new HypoverseWatcher((ServerGamePacketListenerImpl) (Object) this);

    @Override
    public HypoverseWatcher voidstain_hypoidol$getHypoverseWatcher() {
        return this.hypoverseWatcher;
    }

    @Inject(method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V", at = @At("HEAD"))
    private void voidstain_hypoidol$removeFromHypoverseOnTeleport(PositionMoveRotation destination, Set<Relative> relatives, CallbackInfo ci) {
        this.hypoverseWatcher.killHypoPlayer();
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void voidstain_hypoidol$removeFromHypoverseOnDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        this.hypoverseWatcher.killHypoPlayer();
    }
}
