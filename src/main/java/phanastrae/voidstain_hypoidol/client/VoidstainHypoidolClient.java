package phanastrae.voidstain_hypoidol.client;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.client.renderer.RenderTargetTexture;
import phanastrae.voidstain_hypoidol.client.renderer.entity.VoidstainEntityRenderers;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.Optional;
import java.util.OptionalInt;

public class VoidstainHypoidolClient implements ClientModInitializer {

    public static Identifier TARGET_ID = VoidstainHypoidol.id("target");
    public static RenderTarget TARGET;
    public static RenderTargetTexture TARGET_TEXTURE;
    private static boolean TARGET_INITIALISED = false;
    public static boolean TEXTURE_FILLED = false;

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

    @Override
    public void onInitializeClient() {
        VoidstainEntityRenderers.init();

        LevelRenderEvents.END_EXTRACTION.register((_) -> {
            if (TARGET_INITIALISED) {
                // blit without blending
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", TARGET_TEXTURE.getTextureView(), OptionalInt.empty())) {
                    renderPass.setPipeline(BLIT_NO_BLEND);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.bindTexture(
                            "InSampler",
                            Minecraft.getInstance().getMainRenderTarget().getColorTextureView(),
                            RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
                    );
                    renderPass.draw(0, 3);
                }

                TEXTURE_FILLED = true;
            }
        });
    }

    public static void initTarget() {
        if (!TARGET_INITIALISED) {
            TARGET_INITIALISED = true;
            int pixelsPerBlock = 128;
            TARGET = new TextureTarget("Target", 3 * pixelsPerBlock, 3 * pixelsPerBlock, false);
            TARGET_TEXTURE = new RenderTargetTexture(TARGET);
            Minecraft.getInstance().getTextureManager().register(TARGET_ID, TARGET_TEXTURE);
        }
    }
}