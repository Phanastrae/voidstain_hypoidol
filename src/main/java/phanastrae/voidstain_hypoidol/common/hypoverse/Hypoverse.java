package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;
import phanastrae.voidstain_hypoidol.common.network.IdWatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Hypoverse {

    private final RandomSource random = RandomSource.create();
    private final Map<UUID, EldritchCanvas> canvases = new HashMap<>();
    private final Map<UUID, HypoZone> zones = new HashMap<>();
    private final IdWatcher canvasIdWatcher = new IdWatcher();
    private final IdWatcher zoneIdWatcher = new IdWatcher();

    public void tick(boolean runsNormally) {
        this.zones.values().forEach(level -> level.tick(runsNormally));
    }

    public HypoZone getOrCreateZone(UUID id) {
        return this.zones.computeIfAbsent(id, (uuid) -> new HypoZone(this, uuid));
    }

    public void removeZone(UUID uuid) {
        this.zones.remove(uuid);
    }

    @Nullable
    public EldritchCanvas getCanvas(UUID id) {
        return this.canvases.getOrDefault(id, null);
    }

    public EldritchCanvas getOrCreateCanvas(UUID uuid) {
        return this.canvases.computeIfAbsent(uuid, id -> new EldritchCanvas(id, Mth.createInsecureUUID(this.random)));
    }

    public void putCanvas(UUID id, EldritchCanvas canvas) {
        this.canvases.put(id, canvas);
    }

    @Nullable
    public EldritchCanvas removeCanvas(UUID uuid) {
        return this.canvases.remove(uuid);
    }

    public void connectCanvas(UUID uuid) {
        if (this.canvasIdWatcher.startWatchingId(uuid)) {
            UUID zoneId = this.getOrCreateCanvas(uuid).getZoneId();
            if (this.zoneIdWatcher.startWatchingId(zoneId)) {
                this.getOrCreateZone(zoneId);
            }
        }
    }

    public void disconnectCanvas(UUID uuid) {
        if (this.canvasIdWatcher.stopWatchingId(uuid)) {
            EldritchCanvas canvas = this.removeCanvas(uuid);
            if (canvas != null) {
                UUID zoneId = canvas.getZoneId();
                if (this.zoneIdWatcher.stopWatchingId(zoneId)) {
                    this.removeZone(zoneId);
                }
            }
        }
    }

    public void forEachZone(Consumer<HypoZone> consumer) {
        this.zones.values().forEach(consumer);
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

    @Nullable
    public static Hypoverse fromLevel(Level level) {
        if (level instanceof HypoverseAccess hypoverseAccess) {
            return hypoverseAccess.voidstain_hypoidol$getHypoverse();
        } else {
            return null;
        }
    }

    public static Hypoverse fromServer(MinecraftServer server) {
        return ((HypoverseAccess) server).voidstain_hypoidol$getHypoverse();
    }
}
