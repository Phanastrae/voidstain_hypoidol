package phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

import java.util.UUID;

public class LocalPlayerHypoEntity extends ClientPlayerHypoEntity {

    public LocalPlayerHypoEntity(HypoZone zone, UUID playerUUID) {
        super(zone, playerUUID);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.isDown()) {
            this.vy += 0.01f;
        }
        if (options.keyDown.isDown()) {
            this.vy -= 0.01f;
        }
        if (options.keyLeft.isDown()) {
            this.vx -= 0.01f;
        }
        if (options.keyRight.isDown()) {
            this.vx += 0.01f;
        }

        super.tick(runsNormally, onServer, hypoverse);
    }

    @Override
    public boolean isPlayerControlled() {
        return true;
    }
}
