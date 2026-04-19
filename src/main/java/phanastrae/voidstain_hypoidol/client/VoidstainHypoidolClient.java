package phanastrae.voidstain_hypoidol.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import phanastrae.voidstain_hypoidol.client.renderer.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.client.renderer.entity.VoidstainEntityRenderers;

public class VoidstainHypoidolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register((_) -> {
            EldritchCanvasHandler.close();
        });
    }
}