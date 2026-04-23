package phanastrae.voidstain_hypoidol.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.gui.screen.HypoversePlayScreen;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void voidstain_hypoidol$setHypoverseScreen(Screen screen, CallbackInfo ci, @Local(argsOnly = true, name = "screen") LocalRef<Screen> screenRef) {
        if (screen == null && VoidstainHypoidolClient.HYPOVERSE.hypoPlayer != null) {
            screenRef.set(new HypoversePlayScreen());
        }
    }
}
