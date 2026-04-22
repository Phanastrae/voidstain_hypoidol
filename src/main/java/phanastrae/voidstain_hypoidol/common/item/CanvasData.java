package phanastrae.voidstain_hypoidol.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.UUID;

public record CanvasData(UUID uuid, int width, int height) {
    public static final Codec<CanvasData> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    UUIDUtil.CODEC.fieldOf("canvas_uuid").forGetter(CanvasData::uuid),
                    ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(CanvasData::width),
                    ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(CanvasData::height)
            ).apply(i, CanvasData::new)
    );
    public static final StreamCodec<FriendlyByteBuf, CanvasData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            CanvasData::uuid,
            ByteBufCodecs.INT,
            CanvasData::width,
            ByteBufCodecs.INT,
            CanvasData::height,
            CanvasData::new
    );
}
