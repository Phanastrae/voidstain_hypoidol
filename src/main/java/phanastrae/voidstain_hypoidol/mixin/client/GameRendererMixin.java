package phanastrae.voidstain_hypoidol.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.client.renderer.EldritchCanvasHandler;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "resetData", at = @At("HEAD"))
    private void voidstain_hypoidol$resetData(CallbackInfo ci) {
        // called on world leave
        EldritchCanvasHandler.clearCanvases();
    }
}