package phanastrae.voidstain_hypoidol.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import phanastrae.voidstain_hypoidol.client.gui.VoidstainDebugScreenEntries;
import phanastrae.voidstain_hypoidol.client.renderer.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.client.renderer.EldritchCanvasRenderer;
import phanastrae.voidstain_hypoidol.client.renderer.entity.VoidstainEntityRenderers;

public class VoidstainHypoidolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();
        VoidstainDebugScreenEntries.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(VoidstainHypoidolClient::close);

        // called on F3 + A, dimension change, and world join
        InvalidateRenderStateCallback.EVENT.register(EldritchCanvasHandler::clearCanvases);

        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register((context) -> EldritchCanvasHandler.updateCanvases());
    }

    private static void close(Minecraft minecraft) {
        EldritchCanvasHandler.close();
        EldritchCanvasRenderer.close();
    }
}