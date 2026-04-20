package phanastrae.voidstain_hypoidol.common.item;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.function.UnaryOperator;

public class VoidstainDataComponents {
    public static final DataComponentType<CanvasData> CANVAS_DATA = register("canvas_data", b -> b.persistent(CanvasData.CODEC).networkSynchronized(CanvasData.STREAM_CODEC));

    public static void init() {
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE, VoidstainHypoidol.id(name));
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, builder.apply(DataComponentType.builder()).build());
    }
}
