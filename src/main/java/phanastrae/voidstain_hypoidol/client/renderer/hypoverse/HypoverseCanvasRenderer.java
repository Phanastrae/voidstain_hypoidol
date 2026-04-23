package phanastrae.voidstain_hypoidol.client.renderer.hypoverse;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.Projection;
import net.minecraft.util.ARGB;
import org.joml.Matrix4fStack;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.AllCanvasRenderState;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.CanvasRenderState;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.HypoverseRenderState;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;

import java.util.UUID;

public class HypoverseCanvasRenderer {
    private final AllCanvasRenderState allCanvasRenderData = new AllCanvasRenderState();
    private final HypoverseRenderState hypoverseRenderState = new HypoverseRenderState();
    private boolean shouldRender;

    public void markActiveCanvasId(UUID uuid) {
        this.allCanvasRenderData.activeCanvasIds.add(uuid);
    }

    public void extract(DeltaTracker deltaTracker) {
        this.shouldRender = !this.allCanvasRenderData.activeCanvasIds.isEmpty();
        if (this.shouldRender) {
            extractCanvasData(this.allCanvasRenderData);
            HypoverseRenderer.extractHypoverseData(this.hypoverseRenderState, deltaTracker);
        }
    }

    public void render() {
        CanvasTextureHandler.tryClearOldCanvases();
        if (this.shouldRender) {
            CanvasTextureHandler.setActiveCanvaseCount(this.allCanvasRenderData.canvases.size());
            if (!this.allCanvasRenderData.canvases.isEmpty()) {
                renderCanvases(this.allCanvasRenderData, this.hypoverseRenderState);
            }

            this.allCanvasRenderData.reset();
            this.hypoverseRenderState.reset();
        }
    }

    public static void extractCanvasData(AllCanvasRenderState renderState) {
        for (UUID uuid : renderState.activeCanvasIds) {
            EldritchCanvas canvas = VoidstainHypoidolClient.HYPOVERSE.getCanvas(uuid);
            if (canvas != null) {
                CanvasRenderState canvasRenderState = new CanvasRenderState();
                canvasRenderState.canvasId = uuid;
                canvasRenderState.zoneId = canvas.getZoneId();
                canvasRenderState.dimensions = canvas.getDimensions();

                renderState.canvases.add(canvasRenderState);

                CanvasTexture canvasTexture = CanvasTextureHandler.getOrCreateCanvas(uuid, canvas.getDimensions().width, canvas.getDimensions().height);
                canvasTexture.clearChecksSinceLastUse = 0;
            }
        }
    }

    public static void renderCanvases(AllCanvasRenderState allCanvasRenderState, HypoverseRenderState hypoverseRenderState) {
        RenderSystem.backupProjectionMatrix();
        Projection projection = new Projection();

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        for (CanvasRenderState renderState : allCanvasRenderState.canvases) {
            projection.setupOrtho(-1000.0f, 1000.0f, renderState.dimensions.width, renderState.dimensions.height, false);
            RenderSystem.setProjectionMatrix(HypoverseRenderer.CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

            CanvasTexture canvasTexture = CanvasTextureHandler.getCanvas(renderState.canvasId);
            if (canvasTexture != null) {
                renderCanvas(canvasTexture, renderState, hypoverseRenderState);
            }
        }

        modelViewStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    public static void renderCanvas(CanvasTexture canvasTexture, CanvasRenderState canvasRenderState, HypoverseRenderState hypoverseRenderState) {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(canvasTexture.getTargetTexture().getTexture(), ARGB.color(255, 0, 0, 0));

        HypoverseRenderer.tryRenderZone(canvasTexture, canvasRenderState.zoneId, hypoverseRenderState);

        EldritchCanvas.Dimensions dimensions = canvasRenderState.dimensions;
        HypoverseRenderer.drawWithTexture(canvasTexture, HypoverseRenderer.FRAME_IDENTIFIER, (builder) -> {
            HypoverseRenderer.drawQuad(builder, 0, dimensions.width, 0, dimensions.height);
        });

        canvasTexture.markFilled();
    }
}
