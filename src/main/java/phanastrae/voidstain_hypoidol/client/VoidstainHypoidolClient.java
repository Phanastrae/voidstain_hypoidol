package phanastrae.voidstain_hypoidol.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import phanastrae.voidstain_hypoidol.client.gui.debug.VoidstainDebugScreenEntries;
import phanastrae.voidstain_hypoidol.client.hypoverse.ClientHypoverse;
import phanastrae.voidstain_hypoidol.client.network.VoidstainClientPacketListener;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.CanvasTextureHandler;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.HypoverseRenderer;
import phanastrae.voidstain_hypoidol.client.renderer.entity.VoidstainEntityRenderers;

public class VoidstainHypoidolClient implements ClientModInitializer {

    public static ClientHypoverse HYPOVERSE = new ClientHypoverse();

    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();
        VoidstainDebugScreenEntries.init();

        VoidstainClientPacketListener.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(VoidstainHypoidolClient::close);

        // called on F3 + A, dimension change, and world join
        InvalidateRenderStateCallback.EVENT.register(CanvasTextureHandler::clearCanvases);

        LevelRenderEvents.END_EXTRACTION.register((context -> HypoverseRenderer.CANVAS_RENDERER.extract(context.deltaTracker())));

        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register((context) -> HypoverseRenderer.CANVAS_RENDERER.render());

        ClientTickEvents.START_LEVEL_TICK.register((level -> {
            if (level == Minecraft.getInstance().level) { // if (somehow) multiple levels are being ticked, only tick with the main one
                HYPOVERSE.tick(level.tickRateManager().runsNormally());
            }
        }));
    }

    public static void resetData() {
        // called on world leave
        CanvasTextureHandler.clearCanvases();
        HYPOVERSE = new ClientHypoverse();
    }

    private static void close(Minecraft minecraft) {
        CanvasTextureHandler.close();
        HypoverseRenderer.close();
    }
}