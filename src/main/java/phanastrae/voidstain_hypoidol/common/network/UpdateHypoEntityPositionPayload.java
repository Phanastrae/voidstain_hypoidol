package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record UpdateHypoEntityPositionPayload(UUID entityUUID, float x, float y, float vx, float vy) implements CustomPacketPayload {
    public static final Type<UpdateHypoEntityPositionPayload> TYPE = new Type<>(VoidstainHypoidol.id("update_hypoentity_position"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHypoEntityPositionPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            UpdateHypoEntityPositionPayload::entityUUID,
            ByteBufCodecs.FLOAT,
            UpdateHypoEntityPositionPayload::x,
            ByteBufCodecs.FLOAT,
            UpdateHypoEntityPositionPayload::y,
            ByteBufCodecs.FLOAT,
            UpdateHypoEntityPositionPayload::vx,
            ByteBufCodecs.FLOAT,
            UpdateHypoEntityPositionPayload::vy,
            UpdateHypoEntityPositionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
