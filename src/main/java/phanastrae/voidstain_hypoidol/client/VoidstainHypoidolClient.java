package phanastrae.voidstain_hypoidol.client;

import net.fabricmc.api.ClientModInitializer;

public class VoidstainHypoidolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();
    }
}