package phanastrae.voidstain_hypoidol.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.HypoverseRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "resetData", at = @At("HEAD"))
    private void voidstain_hypoidol$resetData(CallbackInfo ci) {
        // called on world leave
        VoidstainHypoidolClient.resetData();
    }

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V"))
    private void voidstain_hypoidol$extractFullscreenHypoverse(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        HypoverseRenderer.FULLSCREEN_RENDERER.extract(deltaTracker);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V"))
    private void voidstain_hypoidol$renderFullscreenHypoverse(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        HypoverseRenderer.FULLSCREEN_RENDERER.render();
    }

    @ModifyVariable(method = "extract", at = @At("STORE"), name = "shouldRenderLevel")
    private boolean voidstain_hypoidol$blockLevelExtraction(boolean shouldRenderLevel) {
        HypoverseRenderer.FULLSCREEN_RENDERER.decideShouldRender();

        if (HypoverseRenderer.FULLSCREEN_RENDERER.shouldRender()) {
            return false;
        } else {
            return shouldRenderLevel;
        }
    }

    @ModifyVariable(method = "render", at = @At("STORE"), name = "shouldRenderLevel")
    private boolean voidstain_hypoidol$blockLevelRendering(boolean shouldRenderLevel) {
        if (HypoverseRenderer.FULLSCREEN_RENDERER.shouldRender()) {
            return false;
        } else {
            return shouldRenderLevel;
        }
    }
}