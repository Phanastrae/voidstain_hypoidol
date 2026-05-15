package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player;

import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

import java.util.UUID;

public class ServerPlayerHypoEntity extends PlayerHypoEntity {

    private final HypoverseWatcher watcher;

    public ServerPlayerHypoEntity(HypoZone zone, HypoverseWatcher watcher, UUID playerUUID) {
        super(zone, playerUUID);
        this.watcher = watcher;
    }

    public HypoverseWatcher getWatcher() {
        return this.watcher;
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        this.watcher.updateDirectlyWatchedZones();
        super.tick(runsNormally, onServer, hypoverse);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        this.watcher.onHypoPlayerRemoval();
    }
}
