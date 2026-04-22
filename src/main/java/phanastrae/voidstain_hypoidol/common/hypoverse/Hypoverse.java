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
    protected final Map<UUID, EldritchCanvas> canvases = new HashMap<>();
    protected final Map<UUID, HypoZone> zones = new HashMap<>();
    protected final Map<UUID, HypoEntity> entities = new HashMap<>();

    public abstract void tick(boolean runsNormally);

    protected void tick(boolean runsNormally, boolean onServer) {
        this.zones.values().forEach(zone -> zone.tick(runsNormally, onServer, this));
        this.entities.values().forEach(entity -> entity.tick(runsNormally, onServer, this));

        if (onServer) {
            this.zones.forEach((uuid, zone) -> {
                if (zone.isClientDirty()) {
                    zone.sendUpdates();
                }
            });

            this.entities.values().forEach(HypoEntity::sendChanges);
        }
    }

    @Nullable
    public HypoZone getZone(UUID uuid) {
        return this.zones.getOrDefault(uuid, null);
    }

    @Nullable
    public HypoZone removeZone(UUID uuid) {
        HypoZone zone = this.zones.remove(uuid);
        if (zone != null) {
            zone.entities.forEach(e -> this.entities.remove(e.getUuid(), e));
        }
        return zone;
    }

    @Nullable
    public EldritchCanvas getCanvas(UUID uuid) {
        return this.canvases.getOrDefault(uuid, null);
    }

    @Nullable
    public EldritchCanvas removeCanvas(UUID uuid) {
        return this.canvases.remove(uuid);
    }

    @Nullable
    public HypoEntity getEntity(UUID uuid) {
        return this.entities.get(uuid);
    }

    public void addEntity(HypoEntity entity) {
        this.entities.put(entity.getUuid(), entity);

        HypoZone zone = entity.getZone();
        zone.entities.add(entity);
        zone.sendToAllWatchers(entity::getAddEntityPayload);
        zone.setDirty();
    }

    public HypoEntity removeEntity(UUID uuid) {
        HypoEntity entity = this.entities.remove(uuid);

        if (entity != null) {
            HypoZone zone = entity.getZone();
            zone.entities.remove(entity);
            zone.sendToAllWatchers(entity::getRemoveEntityPayload);
            zone.setDirty();
        }

        return entity;
    }

    public void forEachZone(Consumer<HypoZone> consumer) {
        this.zones.values().forEach(consumer);
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
