package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record StopWatchingCanvasPayload(UUID uuid) implements CustomPacketPayload {
    public static final Type<StopWatchingCanvasPayload> TYPE = new Type<>(VoidstainHypoidol.id("stop_watching_canvas"));
    public static final StreamCodec<FriendlyByteBuf, StopWatchingCanvasPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            StopWatchingCanvasPayload::uuid,
            StopWatchingCanvasPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
