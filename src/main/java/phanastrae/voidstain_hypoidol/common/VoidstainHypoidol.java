package phanastrae.voidstain_hypoidol.common;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phanastrae.voidstain_hypoidol.common.entity.VoidstainEntityTypes;
import phanastrae.voidstain_hypoidol.common.item.VoidstainItems;

public class VoidstainHypoidol implements ModInitializer {
    public static final String MOD_ID = "voidstain_hypoidol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        VoidstainItems.init();
        VoidstainCreativeTabs.init();

        VoidstainEntityTypes.init();
    }
}