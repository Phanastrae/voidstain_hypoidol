package phanastrae.voidstain_hypoidol.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiAccessor {
    @Invoker
    void invokeExtractChat(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);

    @Invoker
    void invokeExtractTabList(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);

    @Invoker
    void invokeExtractSubtitleOverlay(GuiGraphicsExtractor graphics, boolean deferRendering);
}
