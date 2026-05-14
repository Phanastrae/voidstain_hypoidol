package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.entity.EldritchPaintingEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.ServerPlayerHypoEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HyperGateHypoEntity extends HypoEntity {
    public static final String KEY_TARGET_POS = "target_pos";

    @Nullable
    private GlobalPos targetPaintingPos;

    public HyperGateHypoEntity(HypoEntityType<?> type, HypoZone zone) {
        super(type, zone);
    }

    public HyperGateHypoEntity(HypoZone zone, GlobalPos targetPaintingPos) {
        super(HypoEntityTypes.HYPERGATE, zone);
        this.targetPaintingPos = targetPaintingPos;
    }

    @Override
    public void write(CompoundTag output) {
        super.write(output);
        if (this.targetPaintingPos != null) {
            output.store(KEY_TARGET_POS, GlobalPos.CODEC, this.targetPaintingPos);
        }
    }

    @Override
    public void read(CompoundTag input) {
        super.read(input);
        input.read(KEY_TARGET_POS, GlobalPos.CODEC).ifPresentOrElse(pos -> this.targetPaintingPos = pos, () -> this.targetPaintingPos = null);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        this.vAngle = 0.04f;
        super.tick(runsNormally, onServer, hypoverse);

        if (onServer && this.targetPaintingPos != null && this.random.nextInt(3) == 0) {
            for (HypoEntity entity : this.getZone().getEntitiesInArea(this.x - 0.5f * this.getWidth(), this.x + 0.5f * this.getWidth(), this.y - 0.5f * this.getHeight(), this.y + 0.5f * this.getHeight())) {
                if (entity instanceof ServerPlayerHypoEntity player) {
                    if (player.getTeleportCooldown() <= 0) {
                        this.teleportPlayer(player, this.targetPaintingPos, hypoverse);
                    }
                }
            }
        }
    }

    public void teleportPlayer(ServerPlayerHypoEntity hypoPlayer, GlobalPos target, Hypoverse hypoverse) {
        hypoPlayer.getWatcher().killHypoPlayer();

        ServerPlayer realPlayer = hypoPlayer.getWatcher().getPlayer();
        ServerLevel level = realPlayer.level();
        ServerLevel targetLevel = level.getServer().getLevel(target.dimension());
        if (targetLevel != null) {
            List<EldritchPaintingEntity> paintings = targetLevel.getEntitiesOfClass(EldritchPaintingEntity.class, new AABB(target.pos()).inflate(1));
            paintings = paintings.stream().filter(painting -> {
                Optional<UUID> canvasUUID = painting.getCanvasUUID();
                if (canvasUUID.isEmpty()) {
                    return false;
                }
                EldritchCanvas canvas = hypoverse.getCanvas(canvasUUID.get());
                if (canvas == null) {
                    return false;
                }
                UUID zoneUUID = canvas.getZoneId();
                return zoneUUID.equals(this.getZone().getUuid());
            }).toList();
            if (!paintings.isEmpty()) {
                EldritchPaintingEntity painting = paintings.getFirst();
                Direction direction = painting.getDirection();

                realPlayer.teleport(new TeleportTransition(
                        targetLevel,
                        painting.getPos().getCenter().subtract(0, realPlayer.getBbHeight() / 2, 0),
                        direction.getUnitVec3().multiply(0.6f, 0.6f, 0.6f).add(0, 0.2f, 0),
                        direction.toYRot(),
                        0.0f,
                        Relative.union(Relative.ROTATION, Relative.DELTA),
                        TeleportTransition.PLAY_PORTAL_SOUND
                ));
            }
        }
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.6f;
    }

    @Nullable
    public GlobalPos getTargetPaintingPos() {
        return this.targetPaintingPos;
    }
}
