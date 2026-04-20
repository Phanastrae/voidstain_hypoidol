package phanastrae.voidstain_hypoidol.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EldritchCanvasRenderer {

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

    public static int renderCanvases(Collection<EldritchCanvas> canvases) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        RenderSystem.backupProjectionMatrix();
        Projection projection = new Projection();
        projection.setupOrtho(-1000.0f, 1000.0f, 1.0f, 1.0f, true);
        RenderSystem.setProjectionMatrix(CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

        AtomicInteger rendered = new AtomicInteger();
        canvases.forEach((canvas) -> {
            if (canvas.needsFilling()) {
                rendered.getAndIncrement();
                EldritchCanvasRenderer.render(canvas);
                canvas.markFilled();
            }
        });

        RenderSystem.restoreProjectionMatrix();
        modelViewStack.popMatrix();

        return rendered.get();
    }

    public static void render(EldritchCanvas canvas) {
        Identifier[] backgroundIds = new Identifier[]{
                VoidstainHypoidol.id("textures/entity/canvas/painting/background_0.png"),
                VoidstainHypoidol.id("textures/entity/canvas/painting/background_1.png"),
                VoidstainHypoidol.id("textures/entity/canvas/painting/background_2.png")
        };
        Identifier[] horrorIds = new Identifier[]{
                VoidstainHypoidol.id("textures/entity/canvas/painting/horror_0.png"),
                VoidstainHypoidol.id("textures/entity/canvas/painting/horror_1.png"),
                VoidstainHypoidol.id("textures/entity/canvas/painting/horror_2.png")
        };

        RANDOM.setSeed(413 + canvas.getId().hashCode());

        drawWithTexture(canvas, backgroundIds[RANDOM.nextInt(backgroundIds.length)], (builder) -> {
            drawQuad(builder, 0, 1, 0, 1);
        });

        drawWithTexture(canvas, horrorIds[RANDOM.nextInt(horrorIds.length)], (builder) -> {
            drawQuad(builder, 1 / 6f, 5 / 6f, 1 / 6f, 5 / 6f);
        });

        drawWithTexture(canvas, VoidstainHypoidol.id("textures/entity/canvas/painting/frame.png"), (builder) -> {
            drawQuad(builder, 0, 1, 0, 1);
        });
    }

    private static void drawWithTexture(EldritchCanvas canvas, Identifier textureId, Consumer<BufferBuilder> runnable) {
        RenderType type = CANVAS_RENDER_TYPE.apply(canvas.getId(), textureId);
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
