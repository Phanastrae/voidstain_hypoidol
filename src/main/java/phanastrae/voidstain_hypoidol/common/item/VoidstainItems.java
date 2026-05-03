package phanastrae.voidstain_hypoidol.common.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.function.Function;

public class VoidstainItems {

    public static final Item ELDRITCH_PAINTING = register("eldritch_painting", EldritchPaintingItem::new, p());
    public static final Item LOVE = register("love", p -> new ColoredNameItem(p, 0xE3326A), p());
    public static final Item UNCERTAINTY = register("uncertainty", p -> new ColoredNameItem(p, 0x9FF589), p());
    public static final Item FEAR = register("fear", p -> new ColoredNameItem(p, 0x91EDEA), p());
    public static final Item HATRED = register("hatred", p -> new ColoredNameItem(p, 0x61506E), p());

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