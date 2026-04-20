package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import java.util.HashSet;
import java.util.Set;

public class AllCanvasRenderState {
    public final Set<String> activeCanvasIds = new HashSet<>();

    public void reset() {
        this.activeCanvasIds.clear();
    }
}
