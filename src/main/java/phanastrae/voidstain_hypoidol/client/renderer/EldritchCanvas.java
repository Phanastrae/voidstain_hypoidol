package phanastrae.voidstain_hypoidol.client.renderer;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public class EldritchCanvas {

    private final String id;
    private final Identifier identifier;
    private final TextureTarget target;
    private final RenderTargetTexture targetTexture;

    private boolean needsFilling = false;
    private long lastTimeNeeded = System.nanoTime();

    public EldritchCanvas(String id) {
        this.id = id;
        this.identifier = VoidstainHypoidol.id(id).withPrefix("prefix/");

        int pixelsPerBlock = 64;
        this.target = new TextureTarget("Canvas " + id, 3 * pixelsPerBlock, 3 * pixelsPerBlock, false);
        this.targetTexture = new RenderTargetTexture(this.target);

        Minecraft.getInstance().getTextureManager().register(this.identifier, this.targetTexture);
    }

    public void close() {
        this.target.destroyBuffers();
        this.targetTexture.close();
    }

    public String getId() {
        return this.id;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public TextureTarget getTarget() {
        return this.target;
    }

    public boolean needsFilling() {
        return this.needsFilling;
    }

    public void markNeedsFilling() {
        this.needsFilling = true;
        this.lastTimeNeeded = System.nanoTime();
    }

    public void markFilled() {
        this.needsFilling = false;
    }

    public long timeSinceLastNeeded() {
        return System.nanoTime() - this.lastTimeNeeded;
    }
}
