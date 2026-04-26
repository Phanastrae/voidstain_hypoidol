package phanastrae.voidstain_hypoidol.client.renderer.hypoverse;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.client.renderer.RenderTargetTexture;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public class CanvasTexture {

    private final UUID canvasId;
    private final Identifier textureIdentifier;
    private final TextureTarget target;
    private final RenderTargetTexture colorTexture;
    private final RenderTargetTexture depthTexture;
    private boolean filled = false;

    public int clearChecksSinceLastUse = 0;

    public CanvasTexture(UUID uuid, int width, int height) {
        this.canvasId = uuid;
        this.textureIdentifier = VoidstainHypoidol.id(uuid.toString()).withPrefix("canvas/");

        int pixelsPerBlock = 32;
        this.target = new TextureTarget("Canvas " + uuid, width * pixelsPerBlock, height * pixelsPerBlock, true);
        this.colorTexture = new RenderTargetTexture(this.target, true);
        this.depthTexture = new RenderTargetTexture(this.target, false);

        Minecraft.getInstance().getTextureManager().register(this.textureIdentifier, this.colorTexture);
    }

    public void close() {
        this.target.destroyBuffers();
        this.colorTexture.close();
    }

    public UUID getCanvasId() {
        return this.canvasId;
    }

    public RenderTargetTexture getColorTexture() {
        return this.colorTexture;
    }

    public RenderTargetTexture getDepthTexture() {
        return this.depthTexture;
    }

    public Identifier getTextureIdentifier() {
        return this.textureIdentifier;
    }

    public TextureTarget getTarget() {
        return this.target;
    }

    public void markFilled() {
        this.filled = true;
    }

    public boolean isFilled() {
        return this.filled;
    }
}
