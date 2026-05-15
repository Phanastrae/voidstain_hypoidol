package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void voidstain_hypoidol$blockPushing(Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player && HypoverseWatcher.isPlayerInHypoverse(player)) {
            ci.cancel();
        } else if (((Entity) (Object) this) instanceof Player player && HypoverseWatcher.isPlayerInHypoverse(player)) {
            ci.cancel();
        }
    }
}
