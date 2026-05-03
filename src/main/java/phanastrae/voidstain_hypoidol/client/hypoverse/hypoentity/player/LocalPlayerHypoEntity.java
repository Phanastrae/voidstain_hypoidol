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
        if (this.leftHeld) {
            this.vAngle += 0.004f;
        }
        if (this.rightHeld) {
            this.vAngle -= 0.004f;
        }
        if (this.upHeld) {
            this.vx += 0.01f * -(float)Math.sin(this.angle);
            this.vy += 0.01f * (float)Math.cos(this.angle);
        }
        if (this.downHeld) {
            this.vx -= 0.01f * -(float)Math.sin(this.angle);
            this.vy -= 0.01f * (float)Math.cos(this.angle);
        }

        this.vx *= 0.93f;
        this.vy *= 0.93f;

        super.tick(runsNormally, onServer, hypoverse);
    }

    @Override
    public boolean isPlayerControlled() {
        return true;
    }

    @Override
    public void sendChanges() {
        if (!this.teleported) {
            sendPacket(new MoveHypoPlayerPayload(this.x, this.y, this.vx, this.vy, this.angle, this.vAngle));
        } else {
            sendPacket(new TeleportHypoPlayerPayload(this.zone.uuid, this.x, this.y, this.vx, this.vy, this.angle, this.vAngle));
        }
        this.teleported = false;
    }

    private static void sendPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
