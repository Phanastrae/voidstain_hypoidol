package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record TeleportHypoEntityPayload(UUID entityUUID, UUID zoneUUID, float x, float y, float ox, float oy, float vx,
                                        float vy, float angle, float oAngle, float vAngle) implements CustomPacketPayload {
    public static final Type<TeleportHypoEntityPayload> TYPE = new Type<>(VoidstainHypoidol.id("teleport_hypoentity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportHypoEntityPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            TeleportHypoEntityPayload::entityUUID,
            UUIDUtil.STREAM_CODEC,
            TeleportHypoEntityPayload::zoneUUID,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::x,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::y,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::ox,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::oy,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::vx,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::vy,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::angle,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::oAngle,
            ByteBufCodecs.FLOAT,
            TeleportHypoEntityPayload::vAngle,
            TeleportHypoEntityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
