package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import phanastrae.voidstain_hypoidol.common.network.AddHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;
import phanastrae.voidstain_hypoidol.common.network.StartWatchingHypoZonePayload;
import phanastrae.voidstain_hypoidol.common.network.UpdateHypoZonePayload;

import java.util.*;
import java.util.function.Supplier;

public class HypoZone {

    private final RandomSource random = RandomSource.create();
    private final Hypoverse hypoverse;
    public final UUID uuid;

    public final List<HypoEntity> entities = new ArrayList<>();
    private final Set<HypoverseWatcher> watchers = new HashSet<>();
    private int backgroundId;

    private boolean isDirty;

    public HypoZone(Hypoverse hypoverse, UUID uuid, int backgroundId) {
        this.hypoverse = hypoverse;
        this.uuid = uuid;
        this.random.setSeed(uuid.hashCode());
        this.backgroundId = backgroundId;
    }

    public void tick(boolean runsNormally) {
        this.entities.forEach(e -> e.tick(runsNormally));
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
        this.markDirty();
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public void markNotDirty() {
        this.isDirty = false;
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void addWatcher(HypoverseWatcher watcher) {
        this.watchers.add(watcher);
    }

    public void removeWatcher(HypoverseWatcher watcher) {
        this.watchers.remove(watcher);
    }

    public void addEntity(HypoEntity hypoEntity) {
        this.entities.add(hypoEntity);
        this.sendToAllWatchers(() -> new AddHypoEntityPayload(this.uuid, hypoEntity.horrorId));
    }

    public void updateNewWatcher(HypoverseWatcher watcher) {
        ServerPlayNetworking.send(watcher.getPlayer(), new StartWatchingHypoZonePayload(this.uuid, this.getBackgroundId()));
        for (HypoEntity entity : this.entities) {
            ServerPlayNetworking.send(watcher.getPlayer(), new AddHypoEntityPayload(this.uuid, entity.horrorId));
        }
    }

    public void sendUpdates() {
        this.sendToAllWatchers(() -> new UpdateHypoZonePayload(this.uuid, this.backgroundId));
        this.markNotDirty();
    }

    public void sendToAllWatchers(Supplier<CustomPacketPayload> payloadSupplier) {
        if (!this.watchers.isEmpty()) {
            CustomPacketPayload payload = payloadSupplier.get();
            this.watchers.forEach(watcher -> {
                ServerPlayNetworking.send(watcher.getPlayer(), payload);
            });
        }
    }
}
