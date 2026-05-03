package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void voidstain_hypoidol$removeFromHypoverseOnDeath(DamageSource source, CallbackInfo ci) {
        HypoverseWatcher.fromPlayer((ServerPlayer) (Object) this).killHypoPlayer();
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"))
    private void voidstain_hypoidol$removeFromHypoverseOnTeleport(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        HypoverseWatcher.fromPlayer((ServerPlayer) (Object) this).killHypoPlayer();
    }
}
