package phanastrae.voidstain_hypoidol.common;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import phanastrae.voidstain_hypoidol.VoidstainHypoidol;

public class VoidstainCreativeTabs {

    public static final ResourceKey<CreativeModeTab> VOIDSTAIN_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, VoidstainHypoidol.id(VoidstainHypoidol.MOD_ID)
    );

    public static final CreativeModeTab VOIDSTAIN_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(VoidstainItems.ELDRITCH_PAINTING))
            .title(Component.translatable("creativeTab.voidstain_hypoidol"))
            .displayItems((VoidstainCreativeTabs::setup))
            .build();

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, VOIDSTAIN_TAB_KEY, VOIDSTAIN_TAB);
    }

    public static void setup(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(VoidstainItems.ELDRITCH_PAINTING);
    }
}