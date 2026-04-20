package phanastrae.voidstain_hypoidol.client.hypoverse;

import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

import java.util.UUID;

public class ClientHypoverse extends Hypoverse {

    public void putZone(UUID id, HypoZone zone) {
        this.zones.put(id, zone);
    }

    public void putCanvas(UUID id, EldritchCanvas canvas) {
        this.canvases.put(id, canvas);
    }

    public int getCanvasCount() {
        return this.canvases.size();
    }

    public int getZoneCount() {
        return this.zones.size();
    }

    public int getEntityCount() {
        int entityCount = 0;
        for (HypoZone zone : this.zones.values()) {
            entityCount += zone.entities.size();
        }
        return entityCount;
    }

    public String getStatistics() {
        return "(VsHi) Canvases: " + this.getCanvasCount() + ", H-Zones: " + this.getZoneCount() + ", H-Entities: " + this.getEntityCount();
    }
}
