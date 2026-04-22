package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

import java.util.UUID;

public record StartWatchingHypoZonePayload(UUID uuid, int backgroundId,
                                           HypoZone.Dimensions dimensions) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StartWatchingHypoZonePayload> TYPE = new CustomPacketPayload.Type<>(VoidstainHypoidol.id("start_watching_hypozone"));
    public static final StreamCodec<FriendlyByteBuf, StartWatchingHypoZonePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            StartWatchingHypoZonePayload::uuid,
            ByteBufCodecs.INT,
            StartWatchingHypoZonePayload::backgroundId,
            HypoZone.Dimensions.STREAM_CODEC,
            StartWatchingHypoZonePayload::dimensions,
            StartWatchingHypoZonePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
