package phanastrae.voidstain_hypoidol.client.renderer.hypoverse;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import org.joml.Matrix4fStack;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.HypoverseRenderState;

public class HypoverseFullscreenRenderer {
    private final HypoverseRenderState hypoverseRenderState = new HypoverseRenderState();
    private boolean shouldRender;

    public void decideShouldRender() {
        this.shouldRender = VoidstainHypoidolClient.HYPOVERSE.hypoPlayer != null;
    }

    public boolean shouldRender() {
        return this.shouldRender;
    }

    public void extract(DeltaTracker deltaTracker) {
        if (this.shouldRender) {
            HypoverseRenderer.extractHypoverseData(this.hypoverseRenderState, deltaTracker);
        }
    }

    public void render() {
        if (this.shouldRender) {
            if (this.hypoverseRenderState.mainZoneUUID != null) {
                renderFullscreen(this.hypoverseRenderState);
            }
            this.hypoverseRenderState.reset();
        }
    }

    public static void renderFullscreen(HypoverseRenderState renderState) {
        RenderSystem.backupProjectionMatrix();
        Projection projection = new Projection();

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        Window window = Minecraft.getInstance().getWindow();

        // translate player to screen center
        modelViewStack.translate(0.5f, 0.5f, 0);
        // scale screen
        float scale = 0.25f;
        modelViewStack.scale(scale * window.getHeight() / window.getWidth(), scale, 1);
        // translate bottom left corner to player pos
        modelViewStack.translate(-renderState.playerX, -renderState.playerY, 0);

        projection.setupOrtho(-1000.0f, 1000.0f, 1, 1, false);
        RenderSystem.setProjectionMatrix(HypoverseRenderer.CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
        HypoverseRenderer.tryRenderZone(renderState.mainZoneUUID, renderState);

        modelViewStack.popMatrix();
        RenderSystem.restoreProjectionMatrix();
    }
}
