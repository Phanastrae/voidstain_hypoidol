package phanastrae.voidstain_hypoidol.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.client.renderer.EldritchCanvasHandler;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "renderFrame", at = @At("HEAD"))
    private void voidstain_hypoidol$renderFrame(boolean advanceGameTime, CallbackInfo ci) {
        EldritchCanvasHandler.fillCanvases((Minecraft) (Object) this);
    }
}
