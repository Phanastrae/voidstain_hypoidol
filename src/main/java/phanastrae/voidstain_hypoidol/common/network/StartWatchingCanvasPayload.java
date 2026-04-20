package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record StartWatchingCanvasPayload(UUID uuid, UUID zoneUUID) implements CustomPacketPayload {
    public static final Type<StartWatchingCanvasPayload> TYPE = new Type<>(VoidstainHypoidol.id("start_watching_canvas"));
    public static final StreamCodec<FriendlyByteBuf, StartWatchingCanvasPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            StartWatchingCanvasPayload::uuid,
            UUIDUtil.STREAM_CODEC,
            StartWatchingCanvasPayload::zoneUUID,
            StartWatchingCanvasPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
