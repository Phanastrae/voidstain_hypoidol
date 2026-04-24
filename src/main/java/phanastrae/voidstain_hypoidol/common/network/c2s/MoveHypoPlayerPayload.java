package phanastrae.voidstain_hypoidol.common.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public record MoveHypoPlayerPayload(float x, float y, float vx, float vy) implements CustomPacketPayload {
    public static final Type<MoveHypoPlayerPayload> TYPE = new Type<>(VoidstainHypoidol.id("move_hypoplayer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveHypoPlayerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            MoveHypoPlayerPayload::x,
            ByteBufCodecs.FLOAT,
            MoveHypoPlayerPayload::y,
            ByteBufCodecs.FLOAT,
            MoveHypoPlayerPayload::vx,
            ByteBufCodecs.FLOAT,
            MoveHypoPlayerPayload::vy,
            MoveHypoPlayerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
