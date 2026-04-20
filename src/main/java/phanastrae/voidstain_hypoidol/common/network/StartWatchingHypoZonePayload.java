package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record StartWatchingHypoZonePayload(UUID uuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StartWatchingHypoZonePayload> TYPE = new CustomPacketPayload.Type<>(VoidstainHypoidol.id("start_watching_hypozone"));
    public static final StreamCodec<FriendlyByteBuf, StartWatchingHypoZonePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            StartWatchingHypoZonePayload::uuid,
            StartWatchingHypoZonePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
