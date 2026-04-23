package phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player;

import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;

import java.util.UUID;

public abstract class ClientPlayerHypoEntity extends PlayerHypoEntity {

    public ClientPlayerHypoEntity(HypoZone zone, UUID playerUUID) {
        super(zone, playerUUID);
    }
}
