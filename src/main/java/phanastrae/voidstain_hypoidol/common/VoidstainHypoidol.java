package phanastrae.voidstain_hypoidol.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phanastrae.voidstain_hypoidol.common.entity.VoidstainEntityDataSerializers;
import phanastrae.voidstain_hypoidol.common.entity.VoidstainEntityTypes;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.item.VoidstainItems;
import phanastrae.voidstain_hypoidol.common.network.VoidstainPayloads;

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

        VoidstainEntityDataSerializers.init();
        VoidstainPayloads.init();

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Hypoverse.fromServer(server).tick(server.tickRateManager().runsNormally());
        });
    }
}