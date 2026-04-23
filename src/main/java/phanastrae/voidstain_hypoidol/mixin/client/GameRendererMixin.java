package phanastrae.voidstain_hypoidol.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.HypoverseRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

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

    @Inject(method = "extractGui", at = @At("RETURN"))
    private void voidstain_hypoidol$extractChatForHypoverse(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local GuiGraphicsExtractor graphics) {
        if (HypoverseRenderer.FULLSCREEN_RENDERER.shouldRender()) {
            if (!this.minecraft.options.hideGui) {
                GuiAccessor gui = (GuiAccessor) this.minecraft.gui;
                gui.invokeExtractChat(graphics, deltaTracker);
                gui.invokeExtractTabList(graphics, deltaTracker);
                gui.invokeExtractSubtitleOverlay(graphics, this.minecraft.screen == null || this.minecraft.screen.isInGameUi());
            }
        }
    }
}