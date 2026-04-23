package phanastrae.voidstain_hypoidol.client.hypoverse;

import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.LocalPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;

import java.util.UUID;

public class ClientHypoverse extends Hypoverse {

    @Nullable
    public LocalPlayerHypoEntity hypoPlayer;

    @Override
    public void tick(boolean runsNormally) {
        this.tick(runsNormally, false);
    }

    @Override
    public void addEntity(HypoEntity entity) {
        if (entity instanceof LocalPlayerHypoEntity player) {
            if (this.hypoPlayer != null) {
                VoidstainHypoidol.LOGGER.warn("Received data for a controlled HypoPlayer, but one already exists?");
            }
            this.hypoPlayer = player;
        }
        super.addEntity(entity);
    }

    @Override
    public HypoEntity removeEntity(UUID uuid) {
        if (this.hypoPlayer != null && hypoPlayer.getUuid().equals(uuid)) {
            this.hypoPlayer = null;
        }
        return super.removeEntity(uuid);
    }

    public void putZone(UUID uuid, HypoZone zone) {
        this.activeZones.put(uuid, zone);
    }

    public void putCanvas(UUID uuid, EldritchCanvas canvas) {
        this.activeCanvases.put(uuid, canvas);
    }

    public int getCanvasCount() {
        return this.activeCanvases.size();
    }

    public int getZoneCount() {
        return this.activeZones.size();
    }

    public int getEntityCount() {
        return this.activeEntities.size();
    }

    public int getPortalCount() {
        int portalCount = 0;
        for (HypoZone zone : this.activeZones.values()) {
            portalCount += zone.portals.size();
        }
        return portalCount;
    }

    public String getStatistics() {
        return "(VsHi) Canvases: " + this.getCanvasCount() + ", H-Zones: " + this.getZoneCount() + ", H-Entities: " + this.getEntityCount() + ", Portals: " + this.getPortalCount();
    }
}
