package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.TypedEntityData;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

public abstract class HypoEntity {
    public static final Codec<TypedEntityData<HypoEntityType<?>>> CODEC = TypedEntityData.codec(HypoEntityType.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, TypedEntityData<HypoEntityType<?>>> STREAM_CODEC = TypedEntityData.streamCodec(HypoEntityType.STREAM_CODEC);

    private final RandomSource random = RandomSource.create();

    public float ox;
    public float oy;
    public float x;
    public float y;

    private final HypoEntityType<?> type;
    private HypoZone zone;

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

    public void write(CompoundTag compoundTag) {
    }

    public void read(CompoundTag compoundTag) {
    }

    public void tick(boolean runsNormally) {
        if (runsNormally) {
            this.ox = x;
            this.oy = y;

            this.x += (random.nextFloat() - 0.5f) * 0.125f;
            this.y += (random.nextFloat() - 0.5f) * 0.125f;

            this.x = Math.clamp(this.x, -1.5f, 1.5f);
            this.y = Math.clamp(this.y, -1.5f, 1.5f);
        }
    }
}
