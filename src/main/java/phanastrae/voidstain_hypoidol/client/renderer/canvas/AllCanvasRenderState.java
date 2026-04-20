package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllCanvasRenderState {
    public final Set<String> activeCanvasIds = new HashSet<>();
    public final List<CanvasRenderState> canvasRenderStates = new ArrayList<>();

    public void reset() {
        this.activeCanvasIds.clear();
        this.canvasRenderStates.clear();
    }
}
