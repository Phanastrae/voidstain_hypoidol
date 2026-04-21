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
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix4fStack;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class EldritchCanvasRenderer {
    private static final Identifier[] BACKGROUND_IDENTIFIERS = new Identifier[]{
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_0.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_1.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_2.png")
    };
    private static final Identifier[] HORROR_IDENTIFIERS = new Identifier[]{
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_0.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_1.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_2.png")
    };
    private static final Identifier MORSEL_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/morsel.png");

    public static final AllCanvasRenderState ALL_CANVAS_RENDER_STATE = new AllCanvasRenderState();
    public static final HypoverseRenderState HYPOVERSE_RENDER_STATE = new HypoverseRenderState();

    private static final ProjectionMatrixBuffer CANVAS_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("voidstain_canvas");

    public static final BiFunction<UUID, Identifier, RenderType> CANVAS_RENDER_TYPE = Util.memoize((canvasUuid, textureId) -> {
        OutputTarget target = new OutputTarget("canvas_target", () -> EldritchCanvasHandler.getCanvas(canvasUuid).getTarget());
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
        Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        for (UUID uuid : ALL_CANVAS_RENDER_STATE.activeCanvasIds) {
            EldritchCanvas canvas = hypoverse.getCanvas(uuid);
            if (canvas != null) {
                CanvasRenderState canvasRenderState = new CanvasRenderState();
                canvasRenderState.canvasId = uuid;
                canvasRenderState.zoneId = canvas.getZoneId();

                ALL_CANVAS_RENDER_STATE.canvases.add(canvasRenderState);

                CanvasTexture canvasTexture = EldritchCanvasHandler.getCanvas(uuid);
                canvasTexture.clearChecksSinceLastUse = 0;
            }
        }

        hypoverse.forEachZone(zone -> {
            HypoZoneRenderState levelRenderState = new HypoZoneRenderState();
            levelRenderState.backgroundId = zone.getBackgroundId();

            for (HypoEntity entity : zone.entities) {
                HypoEntityRenderState entityRenderState;
                if (entity instanceof HorrorHypoEntity horrorHypoEntity) {
                    HorrorRenderState state = new HorrorRenderState();
                    state.horrorId = horrorHypoEntity.getHorrorId();

                    entityRenderState = state;
                } else {
                    entityRenderState = new HypoEntityRenderState();
                }

                entityRenderState.x = Mth.lerp(partialTick, entity.ox, entity.x);
                entityRenderState.y = Mth.lerp(partialTick, entity.oy, entity.y);

                levelRenderState.entities.add(entityRenderState);
            }

            HYPOVERSE_RENDER_STATE.zones.put(zone.uuid, levelRenderState);
        });
    }

    public static void render() {
        EldritchCanvasHandler.tryClearOldCanvases();

        EldritchCanvasHandler.setActiveCanvaseCount(ALL_CANVAS_RENDER_STATE.canvases.size());
        if (!ALL_CANVAS_RENDER_STATE.canvases.isEmpty()) {
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

        for (CanvasRenderState renderState : ALL_CANVAS_RENDER_STATE.canvases) {
            CanvasTexture canvasTexture = EldritchCanvasHandler.getCanvas(renderState.canvasId);
            renderCanvas(canvasTexture, renderState);
        }

        RenderSystem.restoreProjectionMatrix();
        modelViewStack.popMatrix();
    }

    public static void renderCanvas(CanvasTexture canvasTexture, CanvasRenderState canvasRenderState) {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(canvasTexture.getTargetTexture().getTexture(), ARGB.color(255, 0, 0, 0));

        if (HYPOVERSE_RENDER_STATE.zones.containsKey(canvasRenderState.zoneId)) {
            HypoZoneRenderState zoneRenderState = HYPOVERSE_RENDER_STATE.zones.get(canvasRenderState.zoneId);

            drawWithTexture(canvasTexture, BACKGROUND_IDENTIFIERS[zoneRenderState.backgroundId], (builder) -> {
                drawQuad(builder, 0, 1, 0, 1);
            });

            for (HypoEntityRenderState entityRenderState : zoneRenderState.entities) {
                float dx = entityRenderState.x / 3;
                float dy = entityRenderState.y / 3;
                if (entityRenderState instanceof HorrorRenderState horrorRenderState) {
                    drawWithTexture(canvasTexture, HORROR_IDENTIFIERS[horrorRenderState.horrorId], (builder) -> {
                        drawQuad(builder, 1 / 3f + dx, 2 / 3f + dx, 1 / 3f + dy, 2 / 3f + dy);
                    });
                } else {
                    drawWithTexture(canvasTexture, MORSEL_IDENTIFIER, (builder) -> {
                        drawQuad(builder, 5 / 12f + dx, 7 / 12f + dx, 5 / 12f + dy, 7 / 12f + dy);
                    });
                }
            }
        }

        drawWithTexture(canvasTexture, VoidstainHypoidol.id("textures/entity/canvas/painting/frame.png"), (builder) -> {
            drawQuad(builder, 0, 1, 0, 1);
        });

        canvasTexture.markFilled();
    }

    private static void drawWithTexture(CanvasTexture canvas, Identifier textureId, Consumer<BufferBuilder> runnable) {
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
