package phanastrae.voidstain_hypoidol.client;

import net.fabricmc.api.ClientModInitializer;
import phanastrae.voidstain_hypoidol.client.renderer.entity.VoidstainEntityRenderers;

public class VoidstainHypoidolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();
    }
}