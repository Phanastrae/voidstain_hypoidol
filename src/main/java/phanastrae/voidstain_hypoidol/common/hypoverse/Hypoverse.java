package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Hypoverse {

    protected final RandomSource random = RandomSource.create();
    protected final Map<UUID, EldritchCanvas> activeCanvases = new HashMap<>();
    protected final Map<UUID, HypoZone> activeZones = new HashMap<>();
    protected final Map<UUID, HypoEntity> activeEntities = new HashMap<>();

    public abstract void tick(boolean runsNormally);

    protected void tick(boolean runsNormally, boolean onServer) {
        this.activeZones.values().forEach(zone -> zone.tick(runsNormally, onServer, this));
        this.activeEntities.values().forEach(entity -> entity.tick(runsNormally, onServer, this));
    }

    @Nullable
    public HypoZone getZone(UUID uuid) {
        return this.activeZones.getOrDefault(uuid, null);
    }

    @Nullable
    public HypoZone removeZone(UUID uuid) {
        HypoZone zone = this.activeZones.remove(uuid);
        if (zone != null) {
            zone.entities.forEach(e -> this.activeEntities.remove(e.getUuid(), e));
            zone.entities.removeIf(e -> !e.getType().canSave());
        }
        return zone;
    }

    @Nullable
    public EldritchCanvas getCanvas(UUID uuid) {
        return this.activeCanvases.getOrDefault(uuid, null);
    }

    @Nullable
    public EldritchCanvas removeCanvas(UUID uuid) {
        return this.activeCanvases.remove(uuid);
    }

    @Nullable
    public HypoEntity getEntity(UUID uuid) {
        return this.activeEntities.get(uuid);
    }

    public void addEntity(HypoEntity entity) {
        this.activeEntities.put(entity.getUuid(), entity);

        HypoZone zone = entity.getZone();
        zone.entities.add(entity);
        zone.sendToAllWatchers(entity::getAddEntityPayload);
        zone.setDirty();
    }

    public HypoEntity removeEntity(UUID uuid) {
        HypoEntity entity = this.activeEntities.remove(uuid);

        if (entity != null) {
            entity.onRemoval();
            HypoZone zone = entity.getZone();
            zone.entities.remove(entity);
            zone.sendToAllWatchers(entity::getRemoveEntityPayload);
            zone.setDirty();
        }

        return entity;
    }

    public void forEachZone(Consumer<HypoZone> consumer) {
        this.activeZones.values().forEach(consumer);
    }

    @Nullable
    public static Hypoverse fromLevel(Level level) {
        if (level instanceof HypoverseAccess hypoverseAccess) {
            return hypoverseAccess.voidstain_hypoidol$getHypoverse();
        } else {
            return null;
        }
    }

    public static ServerHypoverse fromServer(MinecraftServer server) {
        return (ServerHypoverse) ((HypoverseAccess) server).voidstain_hypoidol$getHypoverse();
    }
}
