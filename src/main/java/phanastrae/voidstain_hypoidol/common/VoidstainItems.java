package phanastrae.voidstain_hypoidol.common;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import phanastrae.voidstain_hypoidol.VoidstainHypoidol;

import java.util.function.Function;

public class VoidstainItems {

    public static final Item ELDRITCH_PAINTING = register("eldritch_painting", Item::new, p());

    public static void init() {
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, VoidstainHypoidol.id(name));
        T item = itemFactory.apply(settings.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static Item.Properties p() {
        return new Item.Properties();
    }
}