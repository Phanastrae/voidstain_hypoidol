package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;
import phanastrae.voidstain_hypoidol.common.network.AddHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.RemoveHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.TeleportHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.UpdateHypoEntityPositionPayload;

import java.util.UUID;

public abstract class HypoEntity {
    public static final Codec<TypedEntityData<HypoEntityType<?>>> CODEC = TypedEntityData.codec(HypoEntityType.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, TypedEntityData<HypoEntityType<?>>> STREAM_CODEC = TypedEntityData.streamCodec(HypoEntityType.STREAM_CODEC);

    public static final String KEY_UUID = "UUID";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_VX = "vx";
    public static final String KEY_VY = "vy";

    protected final RandomSource random = RandomSource.create();

    private final HypoEntityType<?> type;
    private HypoZone zone;
    protected UUID uuid = Mth.createInsecureUUID(this.random);

    public float ox;
    public float oy;
    public float x;
    public float y;
    public float vx;
    public float vy;
    private boolean isRemoved = false;

    private int syncTickCount;
    private final int updateInterval = 10;
    private boolean needsSync;
    private boolean teleported = false;
    private HypoZone oldZone = null;

    public HypoEntity(HypoEntityType<?> type, HypoZone zone) {
        this.type = type;
        this.zone = zone;
    }

    public HypoEntityType<?> getType() {
        return this.type;
    }

    public TypedEntityData<HypoEntityType<?>> getData() {
        CompoundTag tag = new CompoundTag();
        this.write(tag);
        return TypedEntityData.of(this.type, tag);
    }

    @Nullable
    public static HypoEntity fromData(HypoZone zone, TypedEntityData<HypoEntityType<?>> data) {
        HypoEntityType<?> type = data.type();
        HypoEntity entity = type.create(zone);
        if (entity != null) {
            entity.read(data.copyTagWithoutId());
            return entity;
        } else {
            VoidstainHypoidol.LOGGER.warn("Failed to create HypoEntity of type {}", type);
            return null;
        }
    }

    public void write(CompoundTag output) {
        output.store(KEY_UUID, UUIDUtil.CODEC, this.getUuid());
        output.store(KEY_X, Codec.FLOAT, this.x);
        output.store(KEY_Y, Codec.FLOAT, this.y);
        output.store(KEY_VX, Codec.FLOAT, this.vx);
        output.store(KEY_VY, Codec.FLOAT, this.vy);
    }

    public void read(CompoundTag input) {
        input.read(KEY_UUID, UUIDUtil.CODEC).ifPresent(id -> {
            this.uuid = id;
        });
        input.read(KEY_X, Codec.FLOAT).ifPresent(x -> {
            this.x = x;
            this.ox = x;
        });
        input.read(KEY_Y, Codec.FLOAT).ifPresent(y -> {
            this.y = y;
            this.oy = y;
        });
        input.read(KEY_VX, Codec.FLOAT).ifPresent(vx -> {
            this.vx = vx;
        });
        input.read(KEY_VY, Codec.FLOAT).ifPresent(vy -> {
            this.vy = vy;
        });
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public HypoZone getZone() {
        return this.zone;
    }

    public void transformCoordinates(Portal from, Portal to) {
        Vec2 newFinalPos = Portal.transformWorldVector(new Vec2(this.x, this.y), from, to);
        this.x = newFinalPos.x;
        this.y = newFinalPos.y;

        Vec2 oldFinalPos = Portal.transformWorldVector(new Vec2(this.ox, this.oy), from, to);
        this.ox = oldFinalPos.x;
        this.oy = oldFinalPos.y;

        Vec2 newVel = Portal.transformRelativeVector(new Vec2(this.vx, this.vy), from, to);
        this.vx = newVel.x;
        this.vy = newVel.y;

        this.needsSync = true;
        this.teleported = true;
    }

    public void travel(Hypoverse hypoverse, Vec2 startPos, Vec2 endPos) {
        Portal fromPortal = null;
        float furthestIntersect = Float.POSITIVE_INFINITY;
        for (Portal portal : this.zone.portals.values()) {
            float intersectDistance = portal.worldRayIntersects(startPos, endPos);
            if (intersectDistance < furthestIntersect) {
                furthestIntersect = intersectDistance;
                fromPortal = portal;
            }
        }

        if (fromPortal != null) {
            HypoZone targetZone = fromPortal.getTargetZone(this.zone, hypoverse);
            if (targetZone != null) {
                Portal toPortal = targetZone.portals.get(fromPortal.getTargetId().portalId);
                if (toPortal != null) {
                    this.transformCoordinates(fromPortal, toPortal);
                    this.setZone(targetZone);
                }
            }
        }

        this.x += this.vx;
        this.y += this.vy;
    }

    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        if (runsNormally) {
            float hWidth = this.getWidth() / 2;
            float hHeight = this.getHeight() / 2;

            this.ox = x;
            this.oy = y;

            this.vx *= 0.99f;
            this.vy *= 0.99f;

            this.travel(hypoverse, new Vec2(this.x, this.y), new Vec2(this.x + this.vx, this.y + this.vy));

            float minX = this.zone.getDimensions().minX + hWidth;
            float maxX = this.zone.getDimensions().maxX - hWidth;
            if (this.x < minX && this.vx < 0) {
                this.x = minX;
                this.vx = -this.vx;
                this.needsSync = true;
            } else if (this.x > maxX && this.vx > 0) {
                this.x = maxX;
                this.vx = -this.vx;
                this.needsSync = true;
            }

            float minY = this.zone.getDimensions().minY + hHeight;
            float maxY = this.zone.getDimensions().maxY - hHeight;
            if (this.y < minY && this.vy < 0) {
                this.y = minY;
                this.vy = -this.vy;
                this.needsSync = true;
            } else if (this.y > maxY && this.vy > 0) {
                this.y = maxY;
                this.vy = -this.vy;
                this.needsSync = true;
            }

            if (onServer && this.random.nextInt(40) == 0) {
                this.vx += (this.random.nextFloat() - 0.5f) * 0.1f;
                this.vy += (this.random.nextFloat() - 0.5f) * 0.1f;
                this.needsSync = true;
            }
        }
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
        this.needsSync = true;
    }

    public void setOldPos(float ox, float oy) {
        this.ox = ox;
        this.oy = oy;
        this.needsSync = true;
    }

    public void setVelocity(float x, float y) {
        this.vx = x;
        this.vy = y;
        this.needsSync = true;
    }

    public void setZone(HypoZone zone) {
        if (this.oldZone == null) {
            this.oldZone = this.zone;
        }

        this.zone.entities.remove(this);
        zone.entities.add(this);
        this.zone = zone;
        this.needsSync = true;
    }

    public void sendChanges() {
        if (this.syncTickCount % this.updateInterval == 0 || this.needsSync) {
            if (!this.teleported) {
                this.zone.sendToAllWatchers(this::getUpdatePositionPayload);
            } else {
                if (this.oldZone == null) {
                    // if zone has not changed, just send a teleport packet
                    this.zone.sendToAllWatchers(this::getTeleportPayload);
                } else {
                    // send add packet to those only watching new
                    this.zone.sendToAllWatchersNotAlsoWatching(this::getAddEntityPayload, this.oldZone.uuid);
                    // send teleport to those watching both
                    this.zone.sendToAllWatchersAlsoWatching(this::getTeleportPayload, this.oldZone.uuid);
                    // send remove to those only watching old
                    this.oldZone.sendToAllWatchersNotAlsoWatching(this::getRemoveEntityPayload, this.zone.uuid);

                    this.oldZone = null;
                }
            }
            this.needsSync = false;
            this.teleported = false;
        }
        this.syncTickCount++;
    }

    public CustomPacketPayload getUpdatePositionPayload() {
        return new UpdateHypoEntityPositionPayload(this.getUuid(), this.x, this.y, this.vx, this.vy);
    }

    public CustomPacketPayload getTeleportPayload() {
        return new TeleportHypoEntityPayload(this.getUuid(), this.zone.uuid, this.x, this.y, this.ox, this.oy, this.vx, this.vy);
    }

    public AddHypoEntityPayload getAddEntityPayload() {
        CompoundTag tag = new CompoundTag();
        this.write(tag);
        return new AddHypoEntityPayload(this.zone.uuid, TypedEntityData.of(this.getType(), tag));
    }

    public RemoveHypoEntityPayload getRemoveEntityPayload() {
        return new RemoveHypoEntityPayload(this.uuid);
    }

    public void playSound(SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        this.zone.playSound(this.x, this.y, soundEvent, source, volume, pitch);
    }

    public void playSound(float x, float y, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        this.zone.playSound(x, y, soundEvent, source, volume, pitch);
    }

    public abstract float getWidth();

    public abstract float getHeight();

    public void setRemoved() {
        this.isRemoved = true;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }
}
