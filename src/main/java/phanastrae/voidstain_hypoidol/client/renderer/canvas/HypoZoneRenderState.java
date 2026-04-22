package phanastrae.voidstain_hypoidol.client.renderer.canvas;

import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

import java.util.ArrayList;
import java.util.List;

public class HypoZoneRenderState {
    public List<HypoEntityRenderState> entities = new ArrayList();
    public List<PortalRenderState> portals = new ArrayList();
    public int backgroundId;
    public HypoZone.Dimensions dimensions;
}
