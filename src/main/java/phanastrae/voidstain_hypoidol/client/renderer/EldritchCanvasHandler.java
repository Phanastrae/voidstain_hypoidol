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
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class EldritchCanvasHandler {

    public static final ProjectionMatrixBuffer CANVAS_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("voidstain_canvas");

    @Nullable
    private static EldritchCanvas CANVAS;

    public static EldritchCanvas getCanvas() {
        if (CANVAS == null) {
            CANVAS = new EldritchCanvas("canvas");
        }
        return CANVAS;
    }

    public static void fillCanvases() {
        EldritchCanvas canvas = CANVAS;
        if (canvas == null) {
            return;
        }
        fillCanvas(canvas);
    }

    public static final BiFunction<EldritchCanvas, Identifier, RenderType> CANVAS_RENDER_TYPE = Util.memoize((canvas, id) -> {
        OutputTarget target = new OutputTarget("canvas_target", canvas::getTarget);
        RenderSetup state = RenderSetup.builder(RenderPipelines.GUI_TEXTURED)
                .withTexture("Sampler0", id)
                .setOutputTarget(target)
                .createRenderSetup();

        return RenderType.create("eldritch_canvas", state);
    });

    public static void fillCanvas(EldritchCanvas canvas) {
        if (!canvas.needsFilling()) {
            canvas.setFilled(false);
            return;
        }
        canvas.setFilled(true);

        RenderSystem.backupProjectionMatrix();
        Projection projection = new Projection();
        projection.setupOrtho(-1000.0f, 1000.0f, 1.0f, 1.0f, true);
        RenderSystem.setProjectionMatrix(CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

        Identifier[] ids = new Identifier[]{
                Identifier.withDefaultNamespace("textures/block/dirt.png"),
                Identifier.withDefaultNamespace("textures/block/cobblestone.png"),
                Identifier.withDefaultNamespace("textures/block/iron_block.png"),
                Identifier.withDefaultNamespace("textures/block/lapis_block.png"),
                Identifier.withDefaultNamespace("textures/block/gold_block.png"),
                Identifier.withDefaultNamespace("textures/block/redstone_block.png"),
                Identifier.withDefaultNamespace("textures/block/emerald_block.png"),
                Identifier.withDefaultNamespace("textures/block/diamond_block.png"),
                Identifier.withDefaultNamespace("textures/block/netherite_block.png")
        };
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                RenderType type = CANVAS_RENDER_TYPE.apply(canvas, ids[x + 3 * y]);

                BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                drawQuad(builder, x / 3f, (x + 1) / 3f, y / 3f, (y + 1) / 3f);
                MeshData mesh = builder.build();
                if (mesh != null) {
                    type.draw(mesh);
                }
            }
        }

        RenderSystem.restoreProjectionMatrix();
    }

    private static void drawQuad(BufferBuilder builder, float x0, float x1, float y0, float y1) {
        builder.addVertex(x0, y0, 0.0f).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(x0, y1, 0.0f).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(x1, y1, 0.0f).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(x1, y0, 0.0f).setUv(1, 0).setColor(255, 255, 255, 255);
    }

    public static void close() {
        if (CANVAS != null) {
            CANVAS.close();
        }
        CANVAS_PROJECTION_MATRIX_BUFFER.close();
    }
}
