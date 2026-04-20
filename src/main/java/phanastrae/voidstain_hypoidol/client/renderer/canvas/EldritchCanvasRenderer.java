package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.joml.Matrix4fStack;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EldritchCanvasRenderer {
    private final static Identifier[] BACKGROUND_IDENTIFIERS = new Identifier[]{
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_0.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_1.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_2.png")
    };
    private final static Identifier[] HORROR_IDENTIFIERS = new Identifier[]{
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_0.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_1.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_2.png")
    };

    public static final AllCanvasRenderState ALL_CANVAS_RENDER_STATE = new AllCanvasRenderState();

    private static final RandomSource RANDOM = RandomSource.create();
    private static final ProjectionMatrixBuffer CANVAS_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("voidstain_canvas");

    public static final BiFunction<String, Identifier, RenderType> CANVAS_RENDER_TYPE = Util.memoize((canvasId, textureId) -> {
        OutputTarget target = new OutputTarget("canvas_target", () -> EldritchCanvasHandler.getCanvas(canvasId).getTarget());
        RenderSetup state = RenderSetup.builder(RenderPipelines.GUI_TEXTURED)
                .withTexture("Sampler0", textureId)
                .setOutputTarget(target)
                .createRenderSetup();

        return RenderType.create("eldritch_canvas", state);
    });

    public static void close() {
        CANVAS_PROJECTION_MATRIX_BUFFER.close();
    }

    public static void extract(DeltaTracker deltaTracker, long gameTime) {
        for (String id : ALL_CANVAS_RENDER_STATE.activeCanvasIds) {
            EldritchCanvas canvas = EldritchCanvasHandler.getCanvas(id);
            canvas.clearChecksSinceLastUse = 0;

            CanvasRenderState canvasRenderState = new CanvasRenderState();
            canvasRenderState.canvasId = canvas.getCanvasId();

            RANDOM.setSeed(413 + canvasRenderState.canvasId.hashCode());
            canvasRenderState.backgroundId = RANDOM.nextInt(BACKGROUND_IDENTIFIERS.length);
            canvasRenderState.horrorId = RANDOM.nextInt(HORROR_IDENTIFIERS.length);
            double theta = Math.PI * RANDOM.nextDouble() + (gameTime + deltaTracker.getGameTimeDeltaPartialTick(false)) * 0.1;
            canvasRenderState.horrorX = (float)Math.cos(theta);
            canvasRenderState.horrorY = (float)Math.sin(theta);

            ALL_CANVAS_RENDER_STATE.canvasRenderStates.add(canvasRenderState);
        }
    }

    public static void render() {
        EldritchCanvasHandler.tryClearOldCanvases();

        EldritchCanvasHandler.setActiveCanvaseCount(ALL_CANVAS_RENDER_STATE.activeCanvasIds.size());
        if (!ALL_CANVAS_RENDER_STATE.activeCanvasIds.isEmpty()) {
            renderCanvases();
        }

        ALL_CANVAS_RENDER_STATE.reset();
    }

    public static void renderCanvases() {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        RenderSystem.backupProjectionMatrix();
        Projection projection = new Projection();
        projection.setupOrtho(-1000.0f, 1000.0f, 1.0f, 1.0f, true);
        RenderSystem.setProjectionMatrix(CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

        for (CanvasRenderState renderState : ALL_CANVAS_RENDER_STATE.canvasRenderStates) {
            renderCanvas(EldritchCanvasHandler.getCanvas(renderState.canvasId), renderState);
        }

        RenderSystem.restoreProjectionMatrix();
        modelViewStack.popMatrix();
    }

    public static void renderCanvas(EldritchCanvas canvas, CanvasRenderState renderState) {
        drawWithTexture(canvas, BACKGROUND_IDENTIFIERS[renderState.backgroundId], (builder) -> {
            drawQuad(builder, 0, 1, 0, 1);
        });

        float dx = renderState.horrorX / 24;
        float dy = renderState.horrorY / 24;
        drawWithTexture(canvas, HORROR_IDENTIFIERS[renderState.horrorId], (builder) -> {
            drawQuad(builder, 1 / 6f + dx, 5 / 6f + dx, 1 / 6f + dy, 5 / 6f + dy);
        });

        drawWithTexture(canvas, VoidstainHypoidol.id("textures/entity/canvas/painting/frame.png"), (builder) -> {
            drawQuad(builder, 0, 1, 0, 1);
        });
    }

    private static void drawWithTexture(EldritchCanvas canvas, Identifier textureId, Consumer<BufferBuilder> runnable) {
        RenderType type = CANVAS_RENDER_TYPE.apply(canvas.getCanvasId(), textureId);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        runnable.accept(builder);
        MeshData mesh = builder.build();
        if (mesh != null) {
            type.draw(mesh);
        }
    }

    private static void drawQuad(BufferBuilder builder, float x0, float x1, float y0, float y1) {
        builder.addVertex(x0, y0, 0.0f).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(x0, y1, 0.0f).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(x1, y1, 0.0f).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(x1, y0, 0.0f).setUv(1, 0).setColor(255, 255, 255, 255);
    }
}
