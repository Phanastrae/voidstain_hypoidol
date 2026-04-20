package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseWatcherAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

import java.util.UUID;

public class HypoverseWatcher {

    // map of canvas ids to count of paintings player can see that shows them
    private final IdWatcher watchedCanvases = new IdWatcher();
    // map of level ids to number of canvases player can see that shows them
    private final IdWatcher watchedZones = new IdWatcher();

    public void startWatchingCanvas(UUID uuid, ServerPlayer player) {
        if (this.watchedCanvases.startWatchingId(uuid)) {
            Hypoverse hypoverse = Hypoverse.fromServer(player.level().getServer());
            EldritchCanvas canvas = hypoverse.getCanvas(uuid);

            if (canvas != null) {
                ServerPlayNetworking.send(player, new StartWatchingCanvasPayload(canvas.getUuid(), canvas.getZoneId()));

                UUID levelUuid = canvas.getZoneId();
                if (this.watchedZones.startWatchingId(levelUuid)) {
                    ServerPlayNetworking.send(player, new StartWatchingHypoZonePayload(levelUuid));
                }
            }
        }
    }

    public void stopWatchingCanvas(UUID uuid, ServerPlayer player) {
        if (this.watchedCanvases.stopWatchingId(uuid)) {
            ServerPlayNetworking.send(player, new StopWatchingCanvasPayload(uuid));

            Hypoverse hypoverse = Hypoverse.fromServer(player.level().getServer());
            EldritchCanvas canvas = hypoverse.getCanvas(uuid);
            if (canvas != null) {
                UUID levelUuid = canvas.getZoneId();
                if (this.watchedZones.stopWatchingId(levelUuid)) {
                    ServerPlayNetworking.send(player, new StopWatchingHypoZonePayload(levelUuid));
                }
            }
        }
    }

    public static HypoverseWatcher fromPlayer(ServerPlayer player) {
        return ((HypoverseWatcherAccess) player.connection).voidstain_hypoidol$getHypoverseWatcher();
    }
}
