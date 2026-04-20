package phanastrae.voidstain_hypoidol.common.hypoverse;

import java.util.HashMap;
import java.util.Map;

public class Hypoverse {

    public final Map<String, HypoLevel> levels = new HashMap<>();

    public void tick(boolean runsNormally) {
        this.levels.values().forEach(level -> level.tick(runsNormally));
    }

    public HypoLevel getOrCreateLevel(String id) {
        return levels.computeIfAbsent(id, (s) -> new HypoLevel(this, s));
    }
}
