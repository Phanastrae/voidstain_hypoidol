package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Hypoverse {

    protected final RandomSource random = RandomSource.create();
    protected final Map<UUID, EldritchCanvas> canvases = new HashMap<>();
    protected final Map<UUID, HypoZone> zones = new HashMap<>();

    public abstract void tick(boolean runsNormally);

    protected void tick(boolean runsNormally, boolean onServer) {
        this.zones.values().forEach(zone -> zone.tick(runsNormally, onServer));
        if (onServer) {
            this.zones.forEach((uuid, zone) -> {
                if (zone.isClientDirty()) {
                    zone.sendUpdates();
                }
            });
        }
    }

    @Nullable
    public HypoZone getZone(UUID id) {
        return this.zones.getOrDefault(id, null);
    }

    public void removeZone(UUID uuid) {
        this.zones.remove(uuid);
    }

    @Nullable
    public EldritchCanvas getCanvas(UUID id) {
        return this.canvases.getOrDefault(id, null);
    }

    @Nullable
    public EldritchCanvas removeCanvas(UUID uuid) {
        return this.canvases.remove(uuid);
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
