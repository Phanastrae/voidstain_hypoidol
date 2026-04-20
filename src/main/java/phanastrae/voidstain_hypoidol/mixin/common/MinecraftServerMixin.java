package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.ServerHypoverse;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements HypoverseAccess {

    @Unique
    @Nullable
    private Hypoverse hypoverse;

    @Inject(method = "createLevels", at = @At("RETURN"))
    private void voidstain_hypoidol$createHypoverse(CallbackInfo ci) {
        this.hypoverse = new ServerHypoverse((MinecraftServer) (Object) this);
    }

    @Override
    public Hypoverse voidstain_hypoidol$getHypoverse() {
        return this.hypoverse;
    }
}
