package phanastrae.voidstain_hypoidol.client.hypoverse;

import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

import java.util.UUID;

public class ClientHypoverse extends Hypoverse {

    @Override
    public void tick(boolean runsNormally) {
        this.tick(runsNormally, false);
    }

    public void putZone(UUID uuid, HypoZone zone) {
        this.zones.put(uuid, zone);
    }

    public void putCanvas(UUID uuid, EldritchCanvas canvas) {
        this.canvases.put(uuid, canvas);
    }

    public int getCanvasCount() {
        return this.canvases.size();
    }

    public int getZoneCount() {
        return this.zones.size();
    }

    public int getEntityCount() {
        return this.entities.size();
    }

    public int getPortalCount() {
        int portalCount = 0;
        for (HypoZone zone : this.zones.values()) {
            portalCount += zone.portals.size();
        }
        return portalCount;
    }

    public String getStatistics() {
        return "(VsHi) Canvases: " + this.getCanvasCount() + ", H-Zones: " + this.getZoneCount() + ", H-Entities: " + this.getEntityCount() + ", Portals: " + this.getPortalCount();
    }
}
