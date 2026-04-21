package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public record RemoveHypoEntityPayload(UUID entityUUID) implements CustomPacketPayload {
    public static final Type<RemoveHypoEntityPayload> TYPE = new Type<>(VoidstainHypoidol.id("remove_hypoentity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveHypoEntityPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            RemoveHypoEntityPayload::entityUUID,
            RemoveHypoEntityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
