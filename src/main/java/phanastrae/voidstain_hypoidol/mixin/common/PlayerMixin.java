package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import phanastrae.voidstain_hypoidol.common.duck.PlayerDuck;

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
}
