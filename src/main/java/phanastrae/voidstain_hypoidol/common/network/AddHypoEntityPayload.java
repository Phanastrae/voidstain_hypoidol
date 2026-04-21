package phanastrae.voidstain_hypoidol.common.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.component.TypedEntityData;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityType;

import java.util.UUID;

public record AddHypoEntityPayload(UUID zoneUUID, TypedEntityData<HypoEntityType<?>> data) implements CustomPacketPayload {
    public static final Type<AddHypoEntityPayload> TYPE = new Type<>(VoidstainHypoidol.id("add_hypoentity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddHypoEntityPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            AddHypoEntityPayload::zoneUUID,
            HypoEntity.STREAM_CODEC,
            AddHypoEntityPayload::data,
            AddHypoEntityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
