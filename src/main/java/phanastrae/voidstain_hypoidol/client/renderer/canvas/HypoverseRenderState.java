package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import java.util.HashMap;
import java.util.Map;

public class HypoverseRenderState {
    public Map<String, HypoLevelRenderState> levels = new HashMap<>();

    public void reset() {
        this.levels.clear();
    }
}
