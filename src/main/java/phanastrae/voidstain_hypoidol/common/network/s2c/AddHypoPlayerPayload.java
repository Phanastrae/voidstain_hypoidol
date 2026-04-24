package phanastrae.voidstain_hypoidol.common.network.s2c;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.component.TypedEntityData;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityType;

import java.util.UUID;

public record AddHypoPlayerPayload(UUID zoneUUID, TypedEntityData<HypoEntityType<?>> data, boolean isLocal) implements CustomPacketPayload {
    public static final Type<AddHypoPlayerPayload> TYPE = new Type<>(VoidstainHypoidol.id("add_hypoplayer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddHypoPlayerPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            AddHypoPlayerPayload::zoneUUID,
            HypoEntity.STREAM_CODEC,
            AddHypoPlayerPayload::data,
            ByteBufCodecs.BOOL,
            AddHypoPlayerPayload::isLocal,
            AddHypoPlayerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
