package phanastrae.voidstain_hypoidol.mixin.common;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;
import phanastrae.voidstain_hypoidol.common.network.s2c.SetEntityInHypoversePayload;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.AFTER))
    private void voidstain_hypoidol$sendBonusPackets(ServerPlayer player, CallbackInfo ci) {
        if (this.entity instanceof Player thisPlayer && HypoverseWatcher.isPlayerInHypoverse(thisPlayer)) {
            ServerPlayNetworking.send(player, new SetEntityInHypoversePayload(this.entity.getId(), true));
        }
    }
}
