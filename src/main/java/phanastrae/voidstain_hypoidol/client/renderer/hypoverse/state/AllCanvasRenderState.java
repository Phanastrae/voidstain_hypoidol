package phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state;

import java.util.*;

public class AllCanvasRenderState {
    public final Set<UUID> activeCanvasIds = new HashSet<>();
    public final List<CanvasRenderState> canvases = new ArrayList<>();

    public void reset() {
        this.activeCanvasIds.clear();
        this.canvases.clear();
    }
}
