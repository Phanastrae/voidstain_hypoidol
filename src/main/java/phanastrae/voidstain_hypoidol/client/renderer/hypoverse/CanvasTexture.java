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
    private final RenderTargetTexture targetTexture;
    private boolean filled = false;

    public int clearChecksSinceLastUse = 0;

    public CanvasTexture(UUID uuid, int width, int height) {
        this.canvasId = uuid;
        this.textureIdentifier = VoidstainHypoidol.id(uuid.toString()).withPrefix("canvas/");

        int pixelsPerBlock = 64;
        this.target = new TextureTarget("Canvas " + uuid, width * pixelsPerBlock, height * pixelsPerBlock, false);
        this.targetTexture = new RenderTargetTexture(this.target);

        Minecraft.getInstance().getTextureManager().register(this.textureIdentifier, this.targetTexture);
    }

    public void close() {
        this.target.destroyBuffers();
        this.targetTexture.close();
    }

    public UUID getCanvasId() {
        return this.canvasId;
    }

    public RenderTargetTexture getTargetTexture() {
        return this.targetTexture;
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
