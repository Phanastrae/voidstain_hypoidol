package phanastrae.voidstain_hypoidol.common.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import phanastrae.voidstain_hypoidol.common.hypoverse.*;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HyperGateHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.MorselHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;
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
    private static final EntityDataAccessor<Integer> DATA_WIDTH = SynchedEntityData.defineId(EldritchPaintingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEIGHT = SynchedEntityData.defineId(EldritchPaintingEntity.class, EntityDataSerializers.INT);

    public static final String KEY_FACING = "facing";
    public static final String KEY_CANVAS_UUID = "canvas_uuid";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";

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
        entityData.define(DATA_WIDTH, 1);
        entityData.define(DATA_HEIGHT, 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_WIDTH.equals(accessor) || DATA_HEIGHT.equals(accessor)) {
            this.recalculateBoundingBox();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store(KEY_FACING, HORIZONTAL_CODEC, this.getDirection());
        this.getCanvasUUID().ifPresent(uuid -> output.store(KEY_CANVAS_UUID, UUIDUtil.CODEC, uuid));
        output.putInt(KEY_WIDTH, this.getWidth());
        output.putInt(KEY_HEIGHT, this.getHeight());
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        Direction direction = input.read(KEY_FACING, HORIZONTAL_CODEC).orElse(Direction.SOUTH);
        input.read(KEY_CANVAS_UUID, UUIDUtil.CODEC).ifPresent(this::setCanvasUUID);
        input.getInt(KEY_WIDTH).ifPresent(this::setWidth);
        input.getInt(KEY_HEIGHT).ifPresent(this::setHeight);
        super.readAdditionalSaveData(input);
        this.setDirection(direction);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        if (type == VoidstainDataComponents.CANVAS_DATA) {
            Optional<UUID> canvasUUID = this.getCanvasUUID();
            return canvasUUID.<T>map(value -> castComponentValue(type, new CanvasData(value, this.getWidth(), this.getHeight()))).orElse(null);
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
            CanvasData data = castComponentValue(VoidstainDataComponents.CANVAS_DATA, value);
            this.setCanvasUUID(data.uuid());
            this.setWidth(data.width());
            this.setHeight(data.height());
            return true;
        }
        return super.applyImplicitComponent(type, value);
    }

    public boolean isEntityAMatchingGate(HypoEntity entity) {
        if (entity instanceof HyperGateHypoEntity hyperGate) {
            GlobalPos pos = hyperGate.getTargetPaintingPos();
            if (pos != null) {
                return pos.pos().equals(this.pos) && pos.dimension().equals(this.level().dimension());
            }
        }

        return false;
    }

    @Override
    public void tick() {
        if (this.connectToHypoverse()) {
            this.getCanvasUUID().ifPresent(canvasUUID -> this.watchingPlayers.forEach(player -> HypoverseWatcher.fromPlayer(player).startWatchingCanvas(canvasUUID, player)));
        }

        if (this.level() instanceof ServerLevel serverLevel && this.random.nextInt(3) == 0) {
            EldritchCanvas canvas = this.getCanvas();
            if (canvas != null) {
                ServerHypoverse hypoverse = ServerHypoverse.fromServer(serverLevel.getServer());
                HypoZone zone = hypoverse.getZone(canvas.getZoneId());
                if (zone != null) {
                    if (zone.entities.stream().anyMatch(this::isEntityAMatchingGate)) {
                        AABB box = this.getBoundingBox();
                        serverLevel.sendParticles(
                                ParticleTypes.REVERSE_PORTAL,
                                this.getX(), this.getY(), this.getZ(),
                                20,
                                (box.maxX - box.minX) / 4, (box.maxY - box.minY) / 4, (box.maxZ - box.minZ) / 4,
                                0.03f
                        );

                        if (!this.isSilent() && this.random.nextInt(25) == 0) {
                            // TODO implement a system to block all real-world noises for hypoverse players, instead of specifically blocking these portal sounds
                            // send portal sound to nearby players, except those inside the hypoverse
                            double x = this.getX();
                            double y = this.getY();
                            double z = this.getZ();
                            Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PORTAL_AMBIENT);
                            float volume = 1.0f;
                            float pitch = 1.0f;
                            long seed = this.random.nextLong();

                            PlayerList playerList = serverLevel.getServer().getPlayerList();
                            List<ServerPlayer> players = playerList.getPlayers();
                            ResourceKey<Level> dimension = serverLevel.dimension();
                            float range = sound.value().getRange(volume);

                            ClientboundSoundPacket packet = new ClientboundSoundPacket(sound, this.getSoundSource(), x, y, z, volume, pitch, seed);
                            for (ServerPlayer player : players) {
                                double xd = x - player.getX();
                                double yd = y - player.getY();
                                double zd = z - player.getZ();
                                if (player.level().dimension() == dimension && xd * xd + yd * yd + zd * zd < range * range && !HypoverseWatcher.fromPlayer(player).hasHypoPlayer()) {
                                    player.connection.send(packet);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean playerInsideTeleportZone(Player player) {
        AABB paintingBox = this.getBoundingBox();

        Direction direction = this.getDirection();
        double expansionSize = player.getBbWidth() - DEPTH;
        // expand box so that thinner side is same width as player
        AABB playerEncloseBox = paintingBox.expandTowards(
                direction.getStepX() * expansionSize,
                0,
                direction.getStepZ() * expansionSize
        );

        Direction.Axis axis = direction.getAxis();
        double thinAdd = 1 / 128.0;
        double thickAdd = 1 / 8.0;
        // inflate box a bit, slightly along the thinner axis and moreso along the other two
        AABB inflatedPlayerEncloseBox = playerEncloseBox.inflate(
                axis == Direction.Axis.X ? thinAdd : thickAdd,
                thickAdd,
                axis == Direction.Axis.Z ? thinAdd : thickAdd
        );

        // shrink box by player dimensions
        AABB playerCenterEnclosePos = inflatedPlayerEncloseBox.deflate(
                player.getBbWidth() / 2,
                player.getBbHeight() / 2,
                player.getBbWidth() / 2
        );

        // check if player center is in the box
        return playerCenterEnclosePos.contains(player.position().add(0, player.getBbHeight() / 2, 0));
    }

    @Override
    public void playerTouch(Player player) {
        if (player instanceof ServerPlayer serverPlayer && this.level() instanceof ServerLevel serverLevel && playerInsideTeleportZone(player)) {
            HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(serverPlayer);
            EldritchCanvas canvas = this.getCanvas();
            if (canvas != null && !watcher.hasHypoPlayer() && !player.isOnPortalCooldown()) {
                ServerHypoverse hypoverse = ServerHypoverse.fromServer(serverLevel.getServer());
                HypoZone zone = hypoverse.getZone(canvas.getZoneId());
                if (zone != null) {
                    List<HypoEntity> gates = zone.entities.stream().filter(this::isEntityAMatchingGate).toList();
                    if (!gates.isEmpty()) {
                        HypoEntity gate = gates.get(this.random.nextInt(gates.size()));
                        if (gate instanceof HyperGateHypoEntity hyperGate) {
                            PlayerHypoEntity hypoPlayer = watcher.createHypoPlayer(hypoverse, zone, hyperGate.x, hyperGate.y, p -> {
                                float angle = this.random.nextFloat() * (float) Math.TAU;
                                p.setVelocity(-Mth.sin(angle) * 0.2f, Mth.cos(angle) * 0.2f);
                                p.setAngle(angle);
                                p.setOldAngle(angle);
                            });
                            hypoPlayer.setTeleportCooldown(50);
                        }
                    }
                }
            }
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

        int width = this.getWidth();
        int height = this.getHeight();

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

        // TODO this is terrible, tidy please
        boolean isBlaze = itemStack.is(Items.BLAZE_POWDER);
        boolean isGhast = itemStack.is(Items.GHAST_TEAR);
        boolean isFood = itemStack.has(DataComponents.FOOD);
        boolean isPearl = itemStack.is(Items.ENDER_PEARL);
        boolean isCanvas = itemStack.is(VoidstainItems.ELDRITCH_PAINTING);
        boolean isCart = itemStack.is(Items.MINECART);
        boolean isDoor = itemStack.is(ItemTags.DOORS);
        boolean isFire = itemStack.is(Items.FIRE_CHARGE);
        boolean isSponge = itemStack.is(Items.SPONGE);
        if (canvasUUID.isPresent() && !itemStack.isEmpty() && (isBlaze || isGhast || isFood || isPearl || isCanvas || isCart || isDoor || isFire || isSponge) && (player.getAbilities().mayBuild || isFood)) {
            if (player.level().isClientSide()) {
                return InteractionResult.SUCCESS_SERVER;
            } else {
                Hypoverse hypoverse = this.getHypoverse();
                EldritchCanvas canvas = this.getCanvas();
                if (hypoverse != null && canvas != null) {
                    HypoZone zone = hypoverse.getZone(canvas.getZoneId());
                    if (zone != null) {
                        Vec2 canvasPos = relativePosToCanvasPos(location);
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
                            hypoEntity.setPos(canvasPos.x, canvasPos.y);
                            hypoEntity.setAngle(this.random.nextFloat() * (float) Math.TAU);
                            hypoEntity.setAngleVelocity((this.random.nextFloat() - 0.5f) * 0.05f);
                            hypoverse.addEntity(hypoEntity);

                            if (!player.hasInfiniteMaterials()) {
                                itemStack.split(1);
                            }
                            return InteractionResult.SUCCESS_SERVER;
                        } else if (isFood) {
                            MorselHypoEntity hypoEntity = new MorselHypoEntity(zone);
                            hypoEntity.setLife(20 * 20);
                            hypoEntity.setPos(canvasPos.x, canvasPos.y);
                            hypoEntity.setAngle(this.random.nextFloat() * (float) Math.TAU);
                            hypoEntity.setAngleVelocity((this.random.nextFloat() - 0.5f) * 0.05f);
                            hypoverse.addEntity(hypoEntity);

                            if (!player.hasInfiniteMaterials()) {
                                itemStack.split(1);
                            }
                            return InteractionResult.SUCCESS_SERVER;
                        } else if (isPearl) {
                            int id1 = zone.getEmptyPortalId(0);
                            int id2 = zone.getEmptyPortalId(id1);

                            Portal portal1 = new Portal(canvasPos.add(new Vec2(0.5f, 0.0f)), 1.0f, this.random.nextFloat() * 360, id1, new Portal.PortalId(zone.uuid, id2));
                            Portal portal2 = new Portal(canvasPos.add(new Vec2(-0.5f, 0.0f)), 1.0f, this.random.nextFloat() * 360, id2, new Portal.PortalId(zone.uuid, id1));
                            zone.addPortal(portal1);
                            zone.addPortal(portal2);

                            if (!player.hasInfiniteMaterials()) {
                                itemStack.split(1);
                            }
                            return InteractionResult.SUCCESS_SERVER;
                        } else if (isCanvas) {
                            CanvasData otherCanvasData = itemStack.get(VoidstainDataComponents.CANVAS_DATA);
                            if (otherCanvasData != null) {
                                EldritchCanvas otherCanvas = hypoverse.getCanvas(otherCanvasData.uuid());
                                if (otherCanvas != null) {
                                    HypoZone otherZone = hypoverse.getZone(otherCanvas.getZoneId());
                                    if (otherZone != null) {
                                        if (zone != otherZone) {
                                            int id1 = zone.getEmptyPortalId(0);
                                            int id2 = otherZone.getEmptyPortalId(id1);

                                            float angle = this.random.nextFloat() * 360;
                                            Vec2 targetPos = new Vec2(
                                                    otherZone.getDimensions().width * canvasPos.x / zone.getDimensions().width,
                                                    otherZone.getDimensions().height * canvasPos.y / zone.getDimensions().height
                                            );

                                            Portal portal1 = new Portal(canvasPos, 1.0f, angle, id1, new Portal.PortalId(otherZone.uuid, id2));
                                            Portal portal2 = new Portal(targetPos, 1.0f, angle, id2, new Portal.PortalId(zone.uuid, id1));
                                            zone.addPortal(portal1);
                                            otherZone.addPortal(portal2);

                                            if (!player.hasInfiniteMaterials()) {
                                                itemStack.split(1);
                                            }
                                            return InteractionResult.SUCCESS_SERVER;
                                        }
                                    }
                                }
                            }
                        } else if (isCart && player instanceof ServerPlayer serverPlayer) {
                            HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(serverPlayer);
                            watcher.createHypoPlayer(hypoverse, zone, canvasPos.x, canvasPos.y, _ -> {
                            });

                            if (!player.hasInfiniteMaterials()) {
                                itemStack.split(1);
                            }
                            return InteractionResult.SUCCESS_SERVER;
                        } else if (isDoor) {
                            HyperGateHypoEntity hypoEntity = new HyperGateHypoEntity(zone, new GlobalPos(this.level().dimension(), this.pos));
                            hypoEntity.setPos(canvasPos.x, canvasPos.y);
                            hypoEntity.setAngle(this.random.nextFloat() * (float) Math.TAU);
                            hypoverse.addEntity(hypoEntity);

                            if (!player.hasInfiniteMaterials()) {
                                itemStack.split(1);
                            }
                            return InteractionResult.SUCCESS_SERVER;
                        } else if (isFire) {
                            // clear all entities
                            zone.entities.forEach(HypoEntity::setRemoved);
                        } else if (isSponge) {
                            // clear all portals
                            List<Portal> portals = zone.portals.values().stream().toList();
                            portals.forEach(p -> zone.removePortal(p.getId()));
                            portals.forEach(p -> {
                                Portal.PortalId target = p.getTargetId();
                                HypoZone targetZone = hypoverse.getZone(target.zoneUUID);
                                if (targetZone != null) {
                                    targetZone.removePortal(target.portalId);
                                }
                            });
                        }
                    }
                }
            }
        }
        return super.interact(player, hand, location);
    }

    @Nullable
    public Hypoverse getHypoverse() {
        return Hypoverse.fromLevel(this.level());
    }

    @Nullable
    public EldritchCanvas getCanvas() {
        Optional<UUID> optionalUUID = this.getCanvasUUID();
        if (optionalUUID.isEmpty()) {
            return null;
        }
        Hypoverse hypoverse = this.getHypoverse();
        if (hypoverse == null) {
            return null;
        }
        return hypoverse.getCanvas(optionalUUID.get());
    }

    public Vec2 relativePosToCanvasPos(Vec3 entityRelativePos) {
        Direction forwardsDirection = this.getDirection();
        Direction canvasXPlusDirection = forwardsDirection.getCounterClockWise();
        Vec3 canvasXVec = canvasXPlusDirection.getUnitVec3();
        Vec3 canvasYVec = Direction.UP.getUnitVec3();

        Vec3 originOffset = entityRelativePos.add(canvasXVec.scale(this.getWidth() / 2f)).add(canvasYVec.scale(this.getHeight() / 2f));

        float canvasX = (float) originOffset.dot(canvasXVec);
        float canvasY = (float) originOffset.dot(canvasYVec);

        return new Vec2(canvasX, canvasY);
    }

    public Vec3 canvasPosToRelativePos(Vec2 canvasPos) {
        Direction forwardsDirection = this.getDirection();
        Direction canvasXPlusDirection = forwardsDirection.getCounterClockWise();
        Vec3 canvasXVec = canvasXPlusDirection.getUnitVec3();
        Vec3 canvasYVec = Direction.UP.getUnitVec3();

        Vec3 originOffset = canvasXVec.scale(canvasPos.x).add(canvasYVec.scale(canvasPos.y));
        return originOffset.subtract(canvasXVec.scale(this.getWidth() / 2f)).subtract(canvasYVec.scale(this.getHeight() / 2f));
    }

    public void playCanvasSound(float x, float y, SoundEvent soundEvent, SoundSource source, float volume,
                                float pitch) {
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
        canvasUUID.ifPresent(value -> stack.set(VoidstainDataComponents.CANVAS_DATA, new CanvasData(value, this.getWidth(), this.getHeight())));
        return stack;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return canBeHurtBy(source) && super.hurtServer(level, source, damage);
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return canBeHurtBy(source) && super.hurtClient(source);
    }

    public boolean canBeHurtBy(DamageSource source) {
        // only allow damage from direct player hits by non-adventure players
        return source.is(DamageTypeTags.IS_PLAYER_ATTACK) && source.getDirectEntity() instanceof Player player && player.getAbilities().mayBuild;
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

    public void setWidth(int width) {
        this.entityData.set(DATA_WIDTH, width);
    }

    public void setHeight(int height) {
        this.entityData.set(DATA_HEIGHT, height);
    }

    public int getWidth() {
        return this.entityData.get(DATA_WIDTH);
    }

    public int getHeight() {
        return this.entityData.get(DATA_HEIGHT);
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
