package phanastrae.voidstain_hypoidol.common.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.Optional;
import java.util.UUID;

public class VoidstainEntityDataSerializers {

    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));

    public static void init() {
        FabricEntityDataRegistry.register(VoidstainHypoidol.id("optional_uuid"), OPTIONAL_UUID);
    }
}
