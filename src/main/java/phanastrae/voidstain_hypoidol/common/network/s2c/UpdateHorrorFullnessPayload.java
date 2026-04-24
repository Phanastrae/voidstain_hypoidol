package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record UpdateHorrorFullnessPayload(UUID entityUUID, float fullness) implements CustomPacketPayload {
    public static final Type<UpdateHorrorFullnessPayload> TYPE = new Type<>(VoidstainHypoidol.id("update_horror_fullness"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHorrorFullnessPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            UpdateHorrorFullnessPayload::entityUUID,
            ByteBufCodecs.FLOAT,
            UpdateHorrorFullnessPayload::fullness,
            UpdateHorrorFullnessPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
