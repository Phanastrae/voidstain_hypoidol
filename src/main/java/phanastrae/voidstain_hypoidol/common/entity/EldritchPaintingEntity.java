package phanastrae.voidstain_hypoidol.common.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.item.VoidstainItems;

import java.util.Optional;

public class EldritchPaintingEntity extends HangingEntity {
    public static final float DEPTH = 0.0625f;
    public static final float HALF_DEPTH = DEPTH / 2;
    private static final float SHIFT_TO_BLOCK_WALL = 0.5F - HALF_DEPTH;
    public static final String KEY_FACING = "facing";
    public static final Codec<Direction> HORIZONTAL_CODEC = Direction.CODEC.validate((v) ->
            v.getAxis().isHorizontal() ? DataResult.success(v) : DataResult.error(() -> "Expected a horizontal direction")
    );

    public EldritchPaintingEntity(EntityType<? extends EldritchPaintingEntity> type, Level level) {
        super(type, level);
    }

    public EldritchPaintingEntity(Level level, BlockPos blockPos) {
        super(VoidstainEntityTypes.ELDRITCH_PAINTING, level, blockPos);
    }

    public static Optional<EldritchPaintingEntity> create(Level level, BlockPos pos, Direction direction) {
        EldritchPaintingEntity candidate = new EldritchPaintingEntity(level, pos);
        candidate.setDirection(direction);
        if (!candidate.survives()) {
            return Optional.empty();
        } else {
            return Optional.of(candidate);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store(KEY_FACING, HORIZONTAL_CODEC, this.getDirection());
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        Direction direction = input.read(KEY_FACING, HORIZONTAL_CODEC).orElse(Direction.SOUTH);
        super.readAdditionalSaveData(input);
        this.setDirection(direction);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        Vec3 attachedToWall = Vec3.atCenterOf(pos).relative(direction, -SHIFT_TO_BLOCK_WALL);

        int width = getWidth();
        int height = getHeight();

        double horizontalOffset = this.offsetForPaintingSize(width);
        double verticalOffset = this.offsetForPaintingSize(height);
        Direction left = direction.getCounterClockWise();
        Vec3 position = attachedToWall.relative(left, horizontalOffset).relative(Direction.UP, verticalOffset);

        Direction.Axis axis = direction.getAxis();
        double xSize = axis == Direction.Axis.X ? DEPTH : width;
        double ySize = height;
        double zSize = axis == Direction.Axis.Z ? DEPTH : width;

        return AABB.ofSize(position, xSize, ySize, zSize);
    }

    private double offsetForPaintingSize(int size) {
        return size % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void dropItem(ServerLevel level, @Nullable Entity causedBy) {
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }

        this.playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);
        if (causedBy instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }

        this.spawnAtLocation(level, VoidstainItems.ELDRITCH_PAINTING);
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void snapTo(double x, double y, double z, float yRot, float xRot) {
        this.setPos(x, y, z);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, this.getDirection().get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setDirection(Direction.from3DDataValue(packet.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(VoidstainItems.ELDRITCH_PAINTING);
    }

    public static int getWidth() {
        return 3;
    }

    public static int getHeight() {
        return 3;
    }
}
