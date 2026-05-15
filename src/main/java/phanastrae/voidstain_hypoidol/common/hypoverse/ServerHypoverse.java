package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.entity.EldritchPaintingEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;
import phanastrae.voidstain_hypoidol.common.network.IdWatcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerHypoverse extends Hypoverse {

    private final MinecraftServer server;

    private final IdWatcher canvasIdWatcher = new IdWatcher();
    private final IdWatcher zoneIdWatcher = new IdWatcher();

    private final Set<HypoverseWatcher> watchersNeedingUpdates = new HashSet<>();

    public ServerHypoverse(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void tick(boolean runsNormally) {
        this.watchersNeedingUpdates.forEach(HypoverseWatcher::updateDirectlyWatchedZones);
        this.watchersNeedingUpdates.clear();

        this.tick(runsNormally, true);

        this.activeZones.forEach((uuid, zone) -> {
            if (zone.isClientDirty()) {
                zone.sendUpdates();
            }
        });

        this.activeEntities.values().forEach(HypoEntity::sendChanges);
    }

    @Override
    public @Nullable HypoZone removeZone(UUID uuid) {
        HypoZone zone = super.removeZone(uuid);
        if (zone != null) {
            zone.entities.forEach(e -> this.activeEntities.remove(e.getUuid(), e));

            zone.removeAllWatchers();

            zone.entities.removeIf(e -> !e.getType().canSave());
        }
        return zone;
    }

    public void connectCanvas(UUID uuid, EldritchPaintingEntity painting) {
        if (this.canvasIdWatcher.startWatchingId(uuid)) {
            EldritchCanvas canvas = this.getOrCreateCanvas(uuid, new EldritchCanvas.Dimensions(painting.getWidth(), painting.getHeight()));
            UUID zoneId = canvas.getZoneId();
            this.startWatchingZone(zoneId, canvas.getDimensions());

            if (this.activeZones.containsKey(zoneId)) {
                this.activeZones.get(zoneId).addLinkedCanvas(canvas);
            }
        }
        if (this.activeCanvases.containsKey(uuid)) {
            this.activeCanvases.get(uuid).addLinkedPainting(painting);
        }
    }

    public void disconnectCanvas(UUID uuid, EldritchPaintingEntity painting) {
        if (this.activeCanvases.containsKey(uuid)) {
            this.activeCanvases.get(uuid).removeLinkedPainting(painting);
        }
        if (this.canvasIdWatcher.stopWatchingId(uuid)) {
            EldritchCanvas canvas = this.removeCanvas(uuid);
            if (canvas != null) {
                UUID zoneId = canvas.getZoneId();
                if (this.activeZones.containsKey(zoneId)) {
                    this.activeZones.get(zoneId).removeLinkedCanvas(canvas);
                }

                this.stopWatchingZone(uuid);
            }
        }
    }

    public void markWatcherNeedsDirectlyWatchedZonesUpdate(HypoverseWatcher watcher) {
        this.watchersNeedingUpdates.add(watcher);
    }

    public void startWatchingZone(UUID zoneId, @Nullable EldritchCanvas.Dimensions dimensions) {
        if (this.zoneIdWatcher.startWatchingId(zoneId) || !this.activeZones.containsKey(zoneId)) {
            if (dimensions != null) {
                this.getOrCreateZone(zoneId, new HypoZone.Dimensions(dimensions.width, dimensions.height));
            } else {
                this.getZoneIfPresent(zoneId);
            }
        }
    }

    public void stopWatchingZone(UUID zoneId) {
        if (this.zoneIdWatcher.stopWatchingId(zoneId)) {
            this.removeZone(zoneId);
        }
    }

    @Nullable
    public HypoZone getZoneIfPresent(UUID zoneUUID) {
        return this.activeZones.computeIfAbsent(zoneUUID, id -> {
            HypoZone zone = this.getZoneFromSavedData(id);
            if (zone != null) {
                zone.setRemoved(false);
                zone.entities.forEach(this::putEntity);
            }
            return zone;
        });
    }

    public HypoZone getOrCreateZone(UUID zoneUUID, HypoZone.Dimensions dimensions) {
        return this.activeZones.computeIfAbsent(zoneUUID, id -> {
            HypoZone zone = this.getOrComputeZoneFromSavedData(id, dimensions);
            zone.setRemoved(false);
            zone.entities.forEach(this::putEntity);
            return zone;
        });
    }

    public EldritchCanvas getOrCreateCanvas(UUID canvasUUID, EldritchCanvas.Dimensions dimensions) {
        return this.activeCanvases.computeIfAbsent(canvasUUID, id -> this.getOrComputeCanvasFromSavedData(id, dimensions));
    }

    @Nullable
    public HypoZone getZoneFromSavedData(UUID zoneUUID) {
        return this.server.getDataStorage().get(HypoZone.type(this, zoneUUID, new HypoZone.Dimensions(1, 1)));
    }

    public HypoZone getOrComputeZoneFromSavedData(UUID zoneUUID, HypoZone.Dimensions dimensions) {
        return this.server.getDataStorage().computeIfAbsent(HypoZone.type(this, zoneUUID, dimensions));
    }

    public EldritchCanvas getOrComputeCanvasFromSavedData(UUID canvasUUID, EldritchCanvas.Dimensions dimensions) {
        return this.server.getDataStorage().computeIfAbsent(EldritchCanvas.type(this, canvasUUID, dimensions));
    }
}
