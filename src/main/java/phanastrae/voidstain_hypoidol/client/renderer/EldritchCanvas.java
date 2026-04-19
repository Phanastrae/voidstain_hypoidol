package phanastrae.voidstain_hypoidol.client.renderer;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public class EldritchCanvas {

    private final Identifier identifier;
    private final TextureTarget target;
    private final RenderTargetTexture targetTexture;

    private boolean needsFilling = false;
    private boolean isFilled = false;

    public EldritchCanvas(String name) {
        this.identifier = VoidstainHypoidol.id(name).withPrefix("prefix/");
        int pixelsPerBlock = 64;
        this.target = new TextureTarget("Canvas " + name, 3 * pixelsPerBlock, 3 * pixelsPerBlock, false);
        this.targetTexture = new RenderTargetTexture(this.target);
        Minecraft.getInstance().getTextureManager().register(this.identifier, this.targetTexture);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public RenderTargetTexture getTexture() {
        return this.targetTexture;
    }

    public boolean needsFilling() {
        return this.needsFilling;
    }

    public void markNeedsFilling() {
        this.needsFilling = true;
    }

    public boolean isFilled() {
        return this.isFilled;
    }

    public void setFilled(boolean filled) {
        this.isFilled = filled;
        if (filled) {
            this.needsFilling = false;
        }
    }
}
