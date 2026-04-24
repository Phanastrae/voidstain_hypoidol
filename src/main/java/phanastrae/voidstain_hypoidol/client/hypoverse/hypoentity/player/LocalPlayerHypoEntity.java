package phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.network.c2s.MoveHypoPlayerPayload;
import phanastrae.voidstain_hypoidol.common.network.c2s.TeleportHypoPlayerPayload;

import java.util.UUID;

public class LocalPlayerHypoEntity extends ClientPlayerHypoEntity {

    public boolean upHeld = false;
    public boolean downHeld = false;
    public boolean leftHeld = false;
    public boolean rightHeld = false;

    public LocalPlayerHypoEntity(HypoZone zone, UUID playerUUID) {
        super(zone, playerUUID);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        if (this.upHeld) {
            this.vy += 0.01f;
        }
        if (this.downHeld) {
            this.vy -= 0.01f;
        }
        if (this.leftHeld) {
            this.vx -= 0.01f;
        }
        if (this.rightHeld) {
            this.vx += 0.01f;
        }

        super.tick(runsNormally, onServer, hypoverse);
    }

    @Override
    public boolean isPlayerControlled() {
        return true;
    }

    @Override
    public void sendChanges() {
        if (!this.teleported) {
            sendPacket(new MoveHypoPlayerPayload(this.x, this.y, this.vx, this.vy));
        } else {
            sendPacket(new TeleportHypoPlayerPayload(this.zone.uuid, this.x, this.y, this.vx, this.vy));
        }
        this.teleported = false;
    }

    private static void sendPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
