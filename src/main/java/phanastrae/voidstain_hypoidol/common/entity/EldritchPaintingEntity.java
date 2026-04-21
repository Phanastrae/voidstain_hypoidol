package phanastrae.voidstain_hypoidol.common.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.ServerHypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.MorselHypoEntity;
import phanastrae.voidstain_hypoidol.common.item.CanvasData;
import phanastrae.voidstain_hypoidol.common.item.VoidstainDataComponents;
import phanastrae.voidstain_hypoidol.common.item.VoidstainItems;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EldritchPaintingEntity extends HangingEntity {
    private static final EntityDataAccessor<Optional<UUID>> DATA_CANVAS_UUID = SynchedEntityData.defineId(EldritchPaintingEntity.class, VoidstainEntityDataSerializers.OPTIONAL_UUID);

    public static final String KEY_FACING = "facing";
    public static final String KEY_CANVAS_UUID = "canvas_uuid";

    public static final float DEPTH = 0.0625f;
    public static final float HALF_DEPTH = DEPTH / 2;
    private static final float SHIFT_TO_BLOCK_WALL = 0.5F - HALF_DEPTH;

    private final List<ServerPlayer> watchingPlayers = new ArrayList<>();
    private boolean connectedToHypoverse = false;

    public static final Codec<Direction> HORIZONTAL_CODEC = Direction.CODEC.validate((v) ->
            v.getAxis().isHorizontal() ? DataResult.success(v) : DataResult.error(() -> "Expected a horizontal direction")
    );

    public EldritchPaintingEntity(EntityType<? extends EldritchPaintingEntity> type, Level level) {
        super(type, level);
        this.setCanvasUUID(Mth.createInsecureUUID(this.random));
    }

    public EldritchPaintingEntity(Level level, BlockPos blockPos) {
        super(VoidstainEntityTypes.ELDRITCH_PAINTING, level, blockPos);
        this.setCanvasUUID(Mth.createInsecureUUID(this.random));
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
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_CANVAS_UUID, Optional.empty());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store(KEY_FACING, HORIZONTAL_CODEC, this.getDirection());
        this.getCanvasUUID().ifPresent(uuid -> output.store(KEY_CANVAS_UUID, UUIDUtil.CODEC, uuid));
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        Direction direction = input.read(KEY_FACING, HORIZONTAL_CODEC).orElse(Direction.SOUTH);
        input.read(KEY_CANVAS_UUID, UUIDUtil.CODEC).ifPresent(this::setCanvasUUID);
        super.readAdditionalSaveData(input);
        this.setDirection(direction);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        if (type == VoidstainDataComponents.CANVAS_DATA) {
            Optional<UUID> canvasUUID = this.getCanvasUUID();
            return canvasUUID.<T>map(value -> castComponentValue(type, value)).orElse(null);
        }
        return super.get(type);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.applyImplicitComponentIfPresent(components, VoidstainDataComponents.CANVAS_DATA);
        super.applyImplicitComponents(components);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
        if (type == VoidstainDataComponents.CANVAS_DATA) {
            this.setCanvasUUID(castComponentValue(VoidstainDataComponents.CANVAS_DATA, value).uuid());
            return true;
        }
        return super.applyImplicitComponent(type, value);
    }

    @Override
    public void tick() {
        if (this.connectToHypoverse()) {
            this.getCanvasUUID().ifPresent(canvasUUID -> this.watchingPlayers.forEach(player -> HypoverseWatcher.fromPlayer(player).startWatchingCanvas(canvasUUID, player)));
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);

        this.watchingPlayers.add(player);
        if (this.connectedToHypoverse) {
            this.getCanvasUUID().ifPresent(uuid -> HypoverseWatcher.fromPlayer(player).startWatchingCanvas(uuid, player));
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);

        this.watchingPlayers.remove(player);
        this.getCanvasUUID().ifPresent(uuid -> HypoverseWatcher.fromPlayer(player).stopWatchingCanvas(uuid, player));
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        this.disconnectFromHypoverse();
        super.onRemoval(reason);
    }

    private boolean connectToHypoverse() {
        if (!this.level().isClientSide() && !this.connectedToHypoverse) {
            this.connectedToHypoverse = true;
            ServerHypoverse hypoverse = Hypoverse.fromServer(((ServerLevel) this.level()).getServer());
            Optional<UUID> uuid = this.getCanvasUUID();
            uuid.ifPresent(id -> hypoverse.connectCanvas(id, this));
            if (uuid.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private void disconnectFromHypoverse() {
        if (!this.level().isClientSide() && this.connectedToHypoverse) {
            this.connectedToHypoverse = false;
            ServerHypoverse hypoverse = Hypoverse.fromServer(((ServerLevel) this.level()).getServer());
            this.getCanvasUUID().ifPresent(id -> hypoverse.disconnectCanvas(id, this));
        }
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
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isSpectator()) {
            return InteractionResult.SUCCESS;
        }
        Optional<UUID> canvasUUID = this.getCanvasUUID();
        boolean isBlaze = itemStack.is(Items.BLAZE_POWDER);
        boolean isGhast = itemStack.is(Items.GHAST_TEAR);
        boolean isFood = itemStack.is(Items.CHORUS_FRUIT);
        if (canvasUUID.isPresent() && !itemStack.isEmpty() && (isBlaze || isGhast || isFood)) {
            if (player.level().isClientSide()) {
                return InteractionResult.SUCCESS_SERVER;
            } else {
                Hypoverse hypoverse = Hypoverse.fromLevel(this.level());
                if (hypoverse != null) {
                    EldritchCanvas canvas = hypoverse.getCanvas(canvasUUID.get());
                    if (canvas != null) {
                        HypoZone zone = hypoverse.getZone(canvas.getZoneId());
                        if (zone != null) {
                            if (isBlaze) {
                                int bgId = zone.getBackgroundId();
                                for (int i = 0; i < 100; i++) {
                                    int newId = this.random.nextInt(3);
                                    if (bgId != newId) {
                                        zone.setBackgroundId(newId);

                                        if (!player.hasInfiniteMaterials()) {
                                            itemStack.split(1);
                                        }
                                        return InteractionResult.SUCCESS_SERVER;
                                    }
                                }
                            } else if (isGhast) {
                                HypoEntity hypoEntity = new HorrorHypoEntity(zone, this.random.nextInt(3));
                                Vec2 pos = relativePosToCanvasPos(location);
                                hypoEntity.setPos(pos.x, pos.y);
                                zone.addEntity(hypoEntity);

                                if (!player.hasInfiniteMaterials()) {
                                    itemStack.split(1);
                                }
                                return InteractionResult.SUCCESS_SERVER;
                            } else if (isFood) {
                                HypoEntity hypoEntity = new MorselHypoEntity(zone);
                                Vec2 pos = relativePosToCanvasPos(location);
                                hypoEntity.setPos(pos.x, pos.y);
                                zone.addEntity(hypoEntity);

                                if (!player.hasInfiniteMaterials()) {
                                    itemStack.split(1);
                                }
                                return InteractionResult.SUCCESS_SERVER;
                            }
                        }
                    }
                }
            }
        }
        return super.interact(player, hand, location);
    }

    public Vec2 relativePosToCanvasPos(Vec3 entityRelativePos) {
        Direction forwardsDirection = this.getDirection();
        Direction canvasXPlusDirection = forwardsDirection.getCounterClockWise();
        Vec3 canvasXVec = canvasXPlusDirection.getUnitVec3();
        Vec3 canvasYVec = Direction.UP.getUnitVec3();
        float width = getWidth();
        float height = getWidth();

        Vec3 originOffset = entityRelativePos.add(canvasXVec.scale(width / 2f)).add(canvasYVec.scale(height / 2f));

        float canvasX = (float) originOffset.dot(canvasXVec);
        float canvasY = (float) originOffset.dot(canvasYVec);

        return new Vec2(canvasX, canvasY);
    }

    public Vec3 canvasPosToRelativePos(Vec2 canvasPos) {
        Direction forwardsDirection = this.getDirection();
        Direction canvasXPlusDirection = forwardsDirection.getCounterClockWise();
        Vec3 canvasXVec = canvasXPlusDirection.getUnitVec3();
        Vec3 canvasYVec = Direction.UP.getUnitVec3();
        float width = getWidth();
        float height = getWidth();

        Vec3 originOffset = canvasXVec.scale(canvasPos.x).add(canvasYVec.scale(canvasPos.y));
        return originOffset.subtract(canvasXVec.scale(width / 2f)).subtract(canvasYVec.scale(height / 2f));
    }

    public void playCanvasSound(float x, float y, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        Vec3 entityRelativePos = canvasPosToRelativePos(new Vec2(x, y));
        Vec3 worldPos = this.position().add(entityRelativePos);
        this.level().playSound(this, worldPos.x, worldPos.y, worldPos.z, soundEvent, source, volume, pitch);
    }

    @Override
    public void dropItem(ServerLevel level, @Nullable Entity causedBy) {
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }

        this.playSound(SoundEvents.PAINTING_BREAK, 1.0f, 1.0f);

        ItemStack dropItem = this.getAsItem();
        if (causedBy instanceof Player player && player.hasInfiniteMaterials() && player.getInventory().contains(dropItem)) {
            return;
        }

        this.spawnAtLocation(level, dropItem);
    }

    public ItemStack getAsItem() {
        ItemStack stack = VoidstainItems.ELDRITCH_PAINTING.getDefaultInstance();
        Optional<UUID> canvasUUID = this.getCanvasUUID();
        canvasUUID.ifPresent(value -> stack.set(VoidstainDataComponents.CANVAS_DATA, new CanvasData(value)));
        return stack;
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
        return this.getAsItem();
    }

    public static int getWidth() {
        return 3;
    }

    public static int getHeight() {
        return 3;
    }

    private void setCanvasUUID(UUID canvasUUID) {
        this.getCanvasUUID().ifPresent(cUUID -> {
            this.watchingPlayers.forEach(player -> HypoverseWatcher.fromPlayer(player).stopWatchingCanvas(cUUID, player));
        });

        this.disconnectFromHypoverse();
        this.entityData.set(DATA_CANVAS_UUID, Optional.of(canvasUUID));
    }

    public Optional<UUID> getCanvasUUID() {
        return this.entityData.get(DATA_CANVAS_UUID);
    }
}
