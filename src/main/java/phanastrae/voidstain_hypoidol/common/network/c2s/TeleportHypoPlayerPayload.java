package phanastrae.voidstain_hypoidol.common.network.c2s;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record TeleportHypoPlayerPayload(UUID zoneUUID, float x, float y, float vx, float vy) implements CustomPacketPayload {
    public static final Type<TeleportHypoPlayerPayload> TYPE = new Type<>(VoidstainHypoidol.id("teleport_hypoplayer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportHypoPlayerPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            TeleportHypoPlayerPayload::zoneUUID,
            ByteBufCodecs.FLOAT,
            TeleportHypoPlayerPayload::x,
            ByteBufCodecs.FLOAT,
            TeleportHypoPlayerPayload::y,
            ByteBufCodecs.FLOAT,
            TeleportHypoPlayerPayload::vx,
            ByteBufCodecs.FLOAT,
            TeleportHypoPlayerPayload::vy,
            TeleportHypoPlayerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
