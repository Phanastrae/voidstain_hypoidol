package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.RandomSource;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;
import phanastrae.voidstain_hypoidol.common.network.UpdateHypoZonePayload;

import java.util.*;

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

        for (int i = 0; i < random.nextIntBetweenInclusive(1, 3); i++) {
            this.entities.add(new HypoEntity(this));
        }
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

    public void sendUpdates() {
        if (!this.watchers.isEmpty()) {
            UpdateHypoZonePayload payload = new UpdateHypoZonePayload(this.uuid, this.backgroundId);
            this.watchers.forEach(watcher -> {
                ServerPlayNetworking.send(watcher.getPlayer(), payload);
            });
        }
        this.markNotDirty();
    }
}
