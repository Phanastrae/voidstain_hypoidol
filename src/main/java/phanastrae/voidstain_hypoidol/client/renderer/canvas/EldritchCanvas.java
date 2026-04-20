package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.client.renderer.RenderTargetTexture;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public class EldritchCanvas {

    private final String canvasId;
    private final Identifier textureIdentifier;
    private final TextureTarget target;
    private final RenderTargetTexture targetTexture;

    public int clearChecksSinceLastUse = 0;

    public EldritchCanvas(String id) {
        this.canvasId = id;
        this.textureIdentifier = VoidstainHypoidol.id(id).withPrefix("canvas/");

        int pixelsPerBlock = 64;
        this.target = new TextureTarget("Canvas " + id, 3 * pixelsPerBlock, 3 * pixelsPerBlock, false);
        this.targetTexture = new RenderTargetTexture(this.target);

        Minecraft.getInstance().getTextureManager().register(this.textureIdentifier, this.targetTexture);
    }

    public void close() {
        this.target.destroyBuffers();
        this.targetTexture.close();
    }

    public String getCanvasId() {
        return this.canvasId;
    }

    public Identifier getTextureIdentifier() {
        return this.textureIdentifier;
    }

    public TextureTarget getTarget() {
        return this.target;
    }
}
