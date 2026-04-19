package phanastrae.voidstain_hypoidol.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.Optional;
import java.util.OptionalInt;

public class EldritchCanvasHandler {

    @Nullable
    private static EldritchCanvas CANVAS;

    public static final BlendFunction COPY = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO);
    public static final RenderPipeline BLIT_NO_BLEND = RenderPipelines.register(RenderPipeline.builder()
            .withLocation(VoidstainHypoidol.id("pipeline/blit_no_blend"))
            .withVertexShader("core/screenquad")
            .withFragmentShader("core/blit_screen")
            .withSampler("InSampler")
            .withColorTargetState(new ColorTargetState(Optional.of(COPY), 7))
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            .build()
    );

    public static EldritchCanvas getCanvas() {
        if (CANVAS == null) {
            CANVAS = new EldritchCanvas("canvas");
        }
        return CANVAS;
    }

    public static void fillCanvases(Minecraft minecraft) {
        EldritchCanvas canvas = CANVAS;
        if (canvas == null) {
            return;
        }
        fillCanvas(canvas, minecraft);
    }

    public static void fillCanvas(EldritchCanvas canvas, Minecraft minecraft) {
        if (!canvas.needsFilling()) {
            canvas.setFilled(false);
            return;
        }
        canvas.setFilled(true);

        // blit without blending
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Blit render target",
                canvas.getTexture().getTextureView(),
                OptionalInt.empty())
        ) {
            renderPass.setPipeline(BLIT_NO_BLEND);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.bindTexture(
                    "InSampler",
                    minecraft.getMainRenderTarget().getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
            );
            renderPass.draw(0, 3);
        }
    }
}
