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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.joml.Matrix4fStack;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoLevel;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

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
    public static final HypoverseRenderState HYPOVERSE_RENDER_STATE = new HypoverseRenderState();

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

    public static void extract(DeltaTracker deltaTracker) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        for (String id : ALL_CANVAS_RENDER_STATE.activeCanvasIds) {
            EldritchCanvas canvas = EldritchCanvasHandler.getCanvas(id);
            canvas.clearChecksSinceLastUse = 0;
        }

        Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
        if (hypoverse != null) {
            for (HypoLevel level : hypoverse.levels.values()) {
                HypoLevelRenderState levelRenderState = new HypoLevelRenderState();
                levelRenderState.backgroundId = level.backgroundId;

                for (HypoEntity entity : level.entities) {
                    HypoEntityRenderState entityRenderState = new HypoEntityRenderState();
                    entityRenderState.x = Mth.lerp(partialTick, entity.ox, entity.x);
                    entityRenderState.y = Mth.lerp(partialTick, entity.oy, entity.y);
                    entityRenderState.horrorId = entity.horrorId;

                    levelRenderState.entities.add(entityRenderState);
                }

                HYPOVERSE_RENDER_STATE.levels.put(level.id, levelRenderState);
            }
        }
    }

    public static void render() {
        EldritchCanvasHandler.tryClearOldCanvases();

        EldritchCanvasHandler.setActiveCanvaseCount(ALL_CANVAS_RENDER_STATE.activeCanvasIds.size());
        if (!ALL_CANVAS_RENDER_STATE.activeCanvasIds.isEmpty()) {
            renderCanvases();
        }

        ALL_CANVAS_RENDER_STATE.reset();
        HYPOVERSE_RENDER_STATE.reset();
    }

    public static void renderCanvases() {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        RenderSystem.backupProjectionMatrix();
        Projection projection = new Projection();
        projection.setupOrtho(-1000.0f, 1000.0f, 1.0f, 1.0f, true);
        RenderSystem.setProjectionMatrix(CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

        for (String canvasId : ALL_CANVAS_RENDER_STATE.activeCanvasIds) {
            if (HYPOVERSE_RENDER_STATE.levels.containsKey(canvasId)) {
                HypoLevelRenderState hypoLevelRenderState = HYPOVERSE_RENDER_STATE.levels.get(canvasId);
                renderCanvas(EldritchCanvasHandler.getCanvas(canvasId), hypoLevelRenderState);
            }
        }

        RenderSystem.restoreProjectionMatrix();
        modelViewStack.popMatrix();
    }

    public static void renderCanvas(EldritchCanvas canvas, HypoLevelRenderState levelRenderState) {
        drawWithTexture(canvas, BACKGROUND_IDENTIFIERS[levelRenderState.backgroundId], (builder) -> {
            drawQuad(builder, 0, 1, 0, 1);
        });

        for (HypoEntityRenderState entityRenderState : levelRenderState.entities) {
            float dx = entityRenderState.x / 3;
            float dy = entityRenderState.y / 3;
            drawWithTexture(canvas, HORROR_IDENTIFIERS[entityRenderState.horrorId], (builder) -> {
                drawQuad(builder, 1 / 3f + dx, 2 / 3f + dx, 1 / 3f + dy, 2 / 3f + dy);
            });
        }

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
