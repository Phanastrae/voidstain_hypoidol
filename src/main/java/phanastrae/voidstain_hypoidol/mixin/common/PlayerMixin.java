package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import phanastrae.voidstain_hypoidol.common.duck.PlayerDuck;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

@Mixin(Player.class)
public abstract class PlayerMixin
        extends Avatar
        implements ContainerUser, PlayerDuck {
    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Unique
    private boolean inHypoverse = false;

    @Override
    public boolean voidstain_hypoidol$isInHypoverse() {
        return this.inHypoverse;
    }

    @Override
    public void voidstain_hypoidol$setInHypoverse(boolean value) {
        this.inHypoverse = value;
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void voidstain_hypoidol$blockDamageInHypoverse(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (HypoverseWatcher.isPlayerInHypoverse((Player) (Object) this) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(HorrorHypoEntity.CONSUMED_DAMAGE_KEY)) {
            cir.setReturnValue(true);
        }
    }
}
