package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.TypedEntityData;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.network.UpdateHypoEntityPositionPayload;

import java.util.UUID;
import java.util.function.Consumer;

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

    public void tick(boolean runsNormally, boolean onServer) {
        if (runsNormally) {
            float hWidth = this.getWidth() / 2;
            float hHeight = this.getHeight() / 2;

            this.ox = x;
            this.oy = y;

            this.vx *= 0.99f;
            this.vy *= 0.99f;

            this.x += this.vx;
            this.y += this.vy;

            if (this.x < hWidth && this.vx < 0) {
                this.x = hWidth;
                this.vx = -this.vx;
                this.needsSync = true;
            }

            if (this.x > 3 - hWidth && this.vx > 0) {
                this.x = 3 - hWidth;
                this.vx = -this.vx;
                this.needsSync = true;
            }

            if (this.y < hHeight && this.vy < 0) {
                this.y = hHeight;
                this.vy = -this.vy;
                this.needsSync = true;
            }

            if (this.y > 3 - hHeight && this.vy > 0) {
                this.y = 3 - hHeight;
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
    }

    public void setVelocity(float x, float y) {
        this.vx = x;
        this.vy = y;
    }

    public void sendChanges(Consumer<CustomPacketPayload> payloadConsumer) {
        if (this.syncTickCount % this.updateInterval == 0 || this.needsSync) {
            payloadConsumer.accept(new UpdateHypoEntityPositionPayload(this.getUuid(), this.x, this.y, this.vx, this.vy));
            this.needsSync = false;
        }
        this.syncTickCount++;
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
