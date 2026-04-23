package phanastrae.voidstain_hypoidol.client.gui.debug;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.hypoverse.ClientHypoverse;

public class DebugEntryHypoverseStats implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
        if (hypoverse != null) {
            displayer.addLine(hypoverse.getStatistics());
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}
