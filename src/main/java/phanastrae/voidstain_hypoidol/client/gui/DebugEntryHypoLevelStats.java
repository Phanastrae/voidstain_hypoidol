package phanastrae.voidstain_hypoidol.client.gui;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoLevel;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

public class DebugEntryHypoLevelStats implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
        if (hypoverse != null) {
            int levelCount = hypoverse.levels.size();
            int entityCount = 0;
            for (HypoLevel level : hypoverse.levels.values()) {
                entityCount += level.entities.size();
            }
            displayer.addLine("(VsHi) H-Levels: " + levelCount + ", H-Entities: " + entityCount);
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}
