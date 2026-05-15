package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;

import java.util.*;
import java.util.function.Consumer;

public abstract class Hypoverse {

    protected final RandomSource random = RandomSource.create();
    protected final Map<UUID, EldritchCanvas> activeCanvases = new HashMap<>();
    protected final Map<UUID, HypoZone> activeZones = new HashMap<>();
    protected final Map<UUID, HypoEntity> activeEntities = new HashMap<>();
    private final List<HypoEntity> queuedEntities = new ArrayList<>();

    public abstract void tick(boolean runsNormally);

    protected void tick(boolean runsNormally, boolean onServer) {
        this.activeZones.values().forEach(zone -> zone.tick(runsNormally, onServer, this));
        this.activeEntities.values().forEach(entity -> {
            if (!entity.isRemoved() && !entity.getZone().isRemoved()) {
                entity.tick(runsNormally, onServer, this);
            }
        });
        this.queuedEntities.forEach(this::addEntity);
        this.queuedEntities.clear();
    }

    @Nullable
    public HypoZone getZone(UUID uuid) {
        return this.activeZones.getOrDefault(uuid, null);
    }

    @Nullable
    public HypoZone removeZone(UUID uuid) {
        HypoZone zone = this.activeZones.remove(uuid);
        if(zone != null) {
            zone.setRemoved(true);
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

    public void queueEntityAddition(HypoEntity hypoEntity) {
        this.queuedEntities.add(hypoEntity);
    }

    public void addEntity(HypoEntity entity) {
        entity.ox = entity.x;
        entity.oy = entity.y;
        entity.oAngle = entity.angle;
        this.putEntity(entity);

        HypoZone zone = entity.getZone();
        zone.entities.add(entity);
        zone.sendToAllWatchers(entity::getAddEntityPayload);
        zone.setDirty();
    }

    public void putEntity(HypoEntity entity) {
        UUID uuid = entity.getUuid();
        if (this.activeEntities.containsKey(uuid)) {
            UUID newUuid = Mth.createInsecureUUID(this.random);
            VoidstainHypoidol.LOGGER.error("Tried to add HypoEntity with pre-existing uuid {}, updating to new uuid {}", uuid, newUuid);
            entity.setUuid(newUuid);
        }

        this.activeEntities.put(entity.getUuid(), entity);
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
