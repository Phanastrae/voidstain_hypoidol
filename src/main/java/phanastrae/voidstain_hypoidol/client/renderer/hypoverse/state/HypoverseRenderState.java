package phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state;

import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion.CameraView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HypoverseRenderState {
    public Map<UUID, HypoZoneRenderState> zones = new HashMap<>();

    @Nullable
    public CameraView cameraView;

    public void reset() {
        this.zones.clear();
    }
}
