package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec2;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;

import java.util.UUID;

public record AddPortalPayload(UUID zoneUUID, Vec2 center, float length, float angle, int id, Portal.PortalId target) implements CustomPacketPayload {
    public static final Type<AddPortalPayload> TYPE = new Type<>(VoidstainHypoidol.id("add_portal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddPortalPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            AddPortalPayload::zoneUUID,
            Portal.VEC2_STREAM_CODEC,
            AddPortalPayload::center,
            ByteBufCodecs.FLOAT,
            AddPortalPayload::length,
            ByteBufCodecs.FLOAT,
            AddPortalPayload::angle,
            ByteBufCodecs.INT,
            AddPortalPayload::id,
            Portal.PortalId.STREAM_CODEC,
            AddPortalPayload::target,
            AddPortalPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
