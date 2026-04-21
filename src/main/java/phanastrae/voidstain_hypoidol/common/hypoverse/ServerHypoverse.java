package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.server.MinecraftServer;
import phanastrae.voidstain_hypoidol.common.entity.EldritchPaintingEntity;
import phanastrae.voidstain_hypoidol.common.network.IdWatcher;

import java.util.UUID;

public class ServerHypoverse extends Hypoverse {

    private final MinecraftServer server;

    private final IdWatcher canvasIdWatcher = new IdWatcher();
    private final IdWatcher zoneIdWatcher = new IdWatcher();

    public ServerHypoverse(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void tick(boolean runsNormally) {
        this.tick(runsNormally, true);
    }

    public void connectCanvas(UUID uuid, EldritchPaintingEntity painting) {
        if (this.canvasIdWatcher.startWatchingId(uuid)) {
            EldritchCanvas canvas = this.getOrCreateCanvas(uuid);
            UUID zoneId = canvas.getZoneId();
            if (this.zoneIdWatcher.startWatchingId(zoneId)) {
                this.getOrCreateZone(zoneId);
            }

            if (this.zones.containsKey(zoneId)) {
                this.zones.get(zoneId).addLinkedCanvas(canvas);
            }
        }
        if (this.canvases.containsKey(uuid)) {
            this.canvases.get(uuid).addLinkedPainting(painting);
        }
    }

    public void disconnectCanvas(UUID uuid, EldritchPaintingEntity painting) {
        if (this.canvases.containsKey(uuid)) {
            this.canvases.get(uuid).removeLinkedPainting(painting);
        }
        if (this.canvasIdWatcher.stopWatchingId(uuid)) {
            EldritchCanvas canvas = this.removeCanvas(uuid);
            if (canvas != null) {
                UUID zoneId = canvas.getZoneId();
                if (this.zones.containsKey(zoneId)) {
                    this.zones.get(zoneId).removeLinkedCanvas(canvas);
                }

                if (this.zoneIdWatcher.stopWatchingId(zoneId)) {
                    this.removeZone(zoneId);
                }
            }
        }
    }

    public HypoZone getOrCreateZone(UUID id) {
        return this.zones.computeIfAbsent(id, this::getOrComputeZoneFromSavedData);
    }

    public EldritchCanvas getOrCreateCanvas(UUID uuid) {
        return this.canvases.computeIfAbsent(uuid, this::getOrComputeCanvasFromSavedData);
    }

    public HypoZone getOrComputeZoneFromSavedData(UUID canvasUUID) {
        return this.server.getDataStorage().computeIfAbsent(HypoZone.type(this, canvasUUID));
    }

    public EldritchCanvas getOrComputeCanvasFromSavedData(UUID canvasUUID) {
        return this.server.getDataStorage().computeIfAbsent(EldritchCanvas.type(this, canvasUUID));
    }
}
