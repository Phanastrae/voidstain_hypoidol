package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HypoverseRenderState {
    public Map<UUID, HypoZoneRenderState> zones = new HashMap<>();

    public void reset() {
        this.zones.clear();
    }
}
