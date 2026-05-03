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
import phanastrae.voidstain_hypoidol.common.network.*;
import phanastrae.voidstain_hypoidol.common.network.s2c.AddHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.RemoveHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.TeleportHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.UpdateHypoEntityPositionPayload;

import java.util.UUID;

public abstract class HypoEntity {
    public static final Codec<TypedEntityData<HypoEntityType<?>>> CODEC = TypedEntityData.codec(HypoEntityType.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, TypedEntityData<HypoEntityType<?>>> STREAM_CODEC = TypedEntityData.streamCodec(HypoEntityType.STREAM_CODEC);

    public static final String KEY_UUID = "UUID";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_VX = "vx";
    public static final String KEY_VY = "vy";
    public static final String KEY_ANGLE = "angle";
    public static final String KEY_V_ANGLE = "v_angle";

    protected final RandomSource random = RandomSource.create();

    private final HypoEntityType<?> type;
    protected HypoZone zone;
    protected UUID uuid = Mth.createInsecureUUID(this.random);

    public float ox;
    public float oy;
    public float x;
    public float y;
    public float vx;
    public float vy;
    public float oAngle;
    public float angle;
    public float vAngle;
    private boolean isRemoved = false;

    private int syncTickCount;
    private final int updateInterval = 10;
    protected boolean needsSync;
    protected boolean teleported = false;
    protected HypoZone oldZone = null;

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
        output.putFloat(KEY_X, this.x);
        output.putFloat(KEY_Y, this.y);
        output.putFloat(KEY_VX, this.vx);
        output.putFloat(KEY_VY, this.vy);
        output.putFloat(KEY_ANGLE, this.angle);
        output.putFloat(KEY_ANGLE, this.vAngle);
    }

    public void read(CompoundTag input) {
        input.read(KEY_UUID, UUIDUtil.CODEC).ifPresent(id -> {
            this.uuid = id;
        });
        input.getFloat(KEY_X).ifPresent(x -> {
            this.x = x;
            this.ox = x;
        });
        input.getFloat(KEY_Y).ifPresent(y -> {
            this.y = y;
            this.oy = y;
        });
        input.getFloat(KEY_VX).ifPresent(vx -> {
            this.vx = vx;
        });
        input.getFloat(KEY_VY).ifPresent(vy -> {
            this.vy = vy;
        });
        input.getFloat(KEY_ANGLE).ifPresent(angle -> {
            this.oAngle = angle;
            this.angle = angle;
        });
        input.getFloat(KEY_V_ANGLE).ifPresent(vAngle -> {
            this.vAngle = vAngle;
        });
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public HypoZone getZone() {
        return this.zone;
    }

    public boolean isPlayerControlled() {
        return false;
    }

    public void transformCoordinates(Portal from, Portal to) {
        Vec2 newFinalPos = Portal.transformZoneVector(new Vec2(this.x, this.y), from, to);
        this.x = newFinalPos.x;
        this.y = newFinalPos.y;

        Vec2 oldFinalPos = Portal.transformZoneVector(new Vec2(this.ox, this.oy), from, to);
        this.ox = oldFinalPos.x;
        this.oy = oldFinalPos.y;

        Vec2 newVel = Portal.transformRelativeVector(new Vec2(this.vx, this.vy), from, to);
        this.vx = newVel.x;
        this.vy = newVel.y;

        float angleDif = (float)Math.toRadians(to.getAngle() - from.getAngle());
        this.oAngle = limitAngleRange(this.oAngle + angleDif);
        this.angle = limitAngleRange(this.angle + angleDif);

        this.needsSync = true;
        this.teleported = true;
    }

    public static float limitAngleRange(float angle) {
        return (float)Mth.positiveModulo(angle, Math.TAU);
    }

    public void travel(Hypoverse hypoverse, Vec2 startPos, Vec2 endPos) {
        Portal fromPortal = null;
        float furthestIntersect = Float.POSITIVE_INFINITY;
        for (Portal portal : this.zone.portals.values()) {
            float intersectDistance = portal.zoneRayIntersects(startPos, endPos);
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
        this.angle = limitAngleRange(this.angle + this.vAngle);
    }

    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        if (runsNormally) {
            float hWidth = this.getWidth() / 2;
            float hHeight = this.getHeight() / 2;

            this.ox = x;
            this.oy = y;
            this.oAngle = angle;

            this.vx *= 0.99f;
            this.vy *= 0.99f;
            this.vAngle *= 0.96f;

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

    public void setAngle(float angle) {
        this.angle = limitAngleRange(angle);
        this.needsSync = true;
    }

    public void setOldAngle(float oAngle) {
        this.oAngle = limitAngleRange(oAngle);
        this.needsSync = true;
    }

    public void setAngleVelocity(float vAngle) {
        this.vAngle = vAngle;
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
        return new UpdateHypoEntityPositionPayload(this.getUuid(), this.x, this.y, this.vx, this.vy, this.angle, this.vAngle);
    }

    public CustomPacketPayload getTeleportPayload() {
        return new TeleportHypoEntityPayload(this.getUuid(), this.zone.uuid, this.x, this.y, this.ox, this.oy, this.vx, this.vy, this.angle, this.oAngle, this.vAngle);
    }

    public CustomPacketPayload getAddEntityPayload(HypoverseWatcher watcher) {
        CompoundTag tag = new CompoundTag();
        this.write(tag);
        return new AddHypoEntityPayload(this.zone.uuid, TypedEntityData.of(this.getType(), tag));
    }

    public RemoveHypoEntityPayload getRemoveEntityPayload() {
        return new RemoveHypoEntityPayload(this.uuid);
    }

    public void playSound(SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        this.playSound(this.x, this.y, soundEvent, source, volume, pitch);
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

    public void onRemoval() {
    }
}
