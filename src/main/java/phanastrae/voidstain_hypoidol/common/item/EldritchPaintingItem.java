package phanastrae.voidstain_hypoidol.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import phanastrae.voidstain_hypoidol.common.entity.EldritchPaintingEntity;
import phanastrae.voidstain_hypoidol.common.entity.VoidstainEntityTypes;

import java.util.Optional;

public class EldritchPaintingItem extends HangingEntityItem {
    public EldritchPaintingItem(Properties properties) {
        super(VoidstainEntityTypes.ELDRITCH_PAINTING, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        HangingEntity entity;
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos blockPos = pos.relative(clickedFace);
        Player player = context.getPlayer();
        ItemStack itemInHand = context.getItemInHand();

        if (player != null && !this.mayPlace(player, clickedFace, itemInHand, blockPos)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        Optional<EldritchPaintingEntity> painting = EldritchPaintingEntity.create(level, blockPos, clickedFace);
        if (painting.isEmpty()) {
            return InteractionResult.CONSUME;
        }
        entity = painting.get();

        EntityType.createDefaultStackConfig(level, itemInHand, player).accept(entity);
        if (entity.survives()) {
            if (!level.isClientSide()) {
                entity.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
                level.addFreshEntity(entity);
            }
            itemInHand.shrink(1);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }
}
