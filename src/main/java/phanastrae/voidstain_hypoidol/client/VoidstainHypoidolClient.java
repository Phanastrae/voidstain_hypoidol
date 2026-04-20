package phanastrae.voidstain_hypoidol.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.client.gui.VoidstainDebugScreenEntries;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasRenderer;
import phanastrae.voidstain_hypoidol.client.renderer.entity.VoidstainEntityRenderers;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

public class VoidstainHypoidolClient implements ClientModInitializer {

    @Nullable
    public static Hypoverse HYPOVERSE;

    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();
        VoidstainDebugScreenEntries.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(VoidstainHypoidolClient::close);

        // called on F3 + A, dimension change, and world join
        InvalidateRenderStateCallback.EVENT.register(EldritchCanvasHandler::clearCanvases);

        LevelRenderEvents.END_EXTRACTION.register((context -> EldritchCanvasRenderer.extract(context.deltaTracker())));

        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register((context) -> EldritchCanvasRenderer.render());

        ClientTickEvents.END_LEVEL_TICK.register((level -> {
            if (HYPOVERSE != null) {
                HYPOVERSE.tick(level.tickRateManager().runsNormally());
            }
        }));
    }

    public static void resetData() {
        // called on world leave
        EldritchCanvasHandler.clearCanvases();
        if (HYPOVERSE != null) {
            HYPOVERSE = null;
        }
    }

    private static void close(Minecraft minecraft) {
        EldritchCanvasHandler.close();
        EldritchCanvasRenderer.close();
    }
}