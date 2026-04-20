package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record AddHypoEntityPayload(UUID zoneUUID, int horrorId) implements CustomPacketPayload {
    public static final Type<AddHypoEntityPayload> TYPE = new Type<>(VoidstainHypoidol.id("add_hypo_entity"));
    public static final StreamCodec<FriendlyByteBuf, AddHypoEntityPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            AddHypoEntityPayload::zoneUUID,
            ByteBufCodecs.INT,
            AddHypoEntityPayload::horrorId,
            AddHypoEntityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
