package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record RemovePortalPayload(UUID zoneUUID, int id) implements CustomPacketPayload {
    public static final Type<RemovePortalPayload> TYPE = new Type<>(VoidstainHypoidol.id("remove_portal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemovePortalPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            RemovePortalPayload::zoneUUID,
            ByteBufCodecs.INT,
            RemovePortalPayload::id,
            RemovePortalPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
