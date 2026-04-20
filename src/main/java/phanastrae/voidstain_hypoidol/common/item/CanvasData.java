package phanastrae.voidstain_hypoidol.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record CanvasData(UUID uuid) {
    public static final Codec<CanvasData> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    UUIDUtil.CODEC.fieldOf("canvas_uuid").forGetter(CanvasData::uuid)
            ).apply(i, CanvasData::new)
    );
    public static final StreamCodec<FriendlyByteBuf, CanvasData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            CanvasData::uuid,
            CanvasData::new
    );
}
