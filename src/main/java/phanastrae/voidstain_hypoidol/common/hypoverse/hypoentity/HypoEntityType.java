package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainRegistries;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

public class HypoEntityType<T extends HypoEntity> {

    public static final Codec<HypoEntityType<?>> CODEC = VoidstainRegistries.HYPOENTITY_TYPE.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, HypoEntityType<?>> STREAM_CODEC = ByteBufCodecs.registry(VoidstainRegistries.HYPOENTITY_TYPE_KEY);

    private final HypoEntityFactory<T> factory;

    public HypoEntityType(HypoEntityFactory<T> factory) {
        this.factory = factory;
    }

    public @Nullable T create(HypoZone zone) {
        return this.factory.create(this, zone);
    }

    @FunctionalInterface
    public interface HypoEntityFactory<T extends HypoEntity> {
        @Nullable T create(HypoEntityType<T> type, HypoZone zone);
    }
}
