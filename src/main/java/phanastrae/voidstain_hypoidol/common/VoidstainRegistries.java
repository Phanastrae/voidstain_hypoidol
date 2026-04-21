package phanastrae.voidstain_hypoidol.common;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityType;

public class VoidstainRegistries {

    public static final ResourceKey<Registry<HypoEntityType<?>>> HYPOENTITY_TYPE_KEY = createRegistryKey("hypoentity_type");
    public static final MappedRegistry<HypoEntityType<?>> HYPOENTITY_TYPE = FabricRegistryBuilder.create(HYPOENTITY_TYPE_KEY).buildAndRegister();

    public static void init() {
    }

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(VoidstainHypoidol.id(name));
    }
}
