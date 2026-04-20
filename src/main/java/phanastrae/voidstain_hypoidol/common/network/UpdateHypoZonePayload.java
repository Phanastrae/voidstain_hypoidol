package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record UpdateHypoZonePayload(UUID uuid, int backgroundId) implements CustomPacketPayload {
    public static final Type<UpdateHypoZonePayload> TYPE = new Type<>(VoidstainHypoidol.id("update_hypozone"));
    public static final StreamCodec<FriendlyByteBuf, UpdateHypoZonePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            UpdateHypoZonePayload::uuid,
            ByteBufCodecs.INT,
            UpdateHypoZonePayload::backgroundId,
            UpdateHypoZonePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
