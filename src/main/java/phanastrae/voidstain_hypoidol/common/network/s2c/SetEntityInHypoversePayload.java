package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public record SetEntityInHypoversePayload(int entityId, boolean value) implements CustomPacketPayload {
    public static final Type<SetEntityInHypoversePayload> TYPE = new Type<>(VoidstainHypoidol.id("set_entity_in_hypoverse"));
    public static final StreamCodec<FriendlyByteBuf, SetEntityInHypoversePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SetEntityInHypoversePayload::entityId,
            ByteBufCodecs.BOOL,
            SetEntityInHypoversePayload::value,
            SetEntityInHypoversePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
