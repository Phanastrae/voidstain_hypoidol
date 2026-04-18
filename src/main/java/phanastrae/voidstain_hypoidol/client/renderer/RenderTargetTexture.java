package phanastrae.voidstain_hypoidol.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class RenderTargetTexture extends AbstractTexture {

    private final RenderTarget target;

    public RenderTargetTexture(RenderTarget target) {
        this.target = target;
        this.sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.NEAREST, FilterMode.NEAREST, false);
    }

    @Override
    public GpuTexture getTexture() {
        GpuTexture texture = this.target.getColorTexture();
        if (texture == null) {
            throw new IllegalStateException("Texture does not exist!");
        } else {
            return target.getColorTexture();
        }
    }

    @Override
    public GpuTextureView getTextureView() {
        GpuTextureView textureView = target.getColorTextureView();
        if (textureView == null) {
            throw new IllegalStateException("Texture view does not exist!");
        } else {
            return textureView;
        }
    }
}
