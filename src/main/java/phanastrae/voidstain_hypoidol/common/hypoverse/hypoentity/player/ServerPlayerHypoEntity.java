package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player;

import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

import java.util.UUID;

public class ServerPlayerHypoEntity extends PlayerHypoEntity {

    private final HypoverseWatcher watcher;

    public ServerPlayerHypoEntity(HypoZone zone, HypoverseWatcher watcher, UUID playerUUID) {
        super(zone, playerUUID);
        this.watcher = watcher;
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        this.watcher.onHypoPlayerRemoval();
    }
}
