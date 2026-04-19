package phanastrae.voidstain_hypoidol.client.gui;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public class VoidstainDebugScreenEntries {
    public static final Identifier CANVAS_RENDER_STATS = register("canvas_render_stats", new DebugEntryCanvasRenderStats());

    public static void init() {
    }

    private static Identifier register(String id, DebugScreenEntry entry) {
        return DebugScreenEntries.register(VoidstainHypoidol.id(id), entry);
    }
}
