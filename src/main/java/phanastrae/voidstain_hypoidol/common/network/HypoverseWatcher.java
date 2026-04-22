package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseWatcherAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

import java.util.UUID;

public class HypoverseWatcher {

    // map of canvas ids to count of paintings player can see that shows them
    private final IdWatcher watchedCanvases = new IdWatcher();
    // map of level ids to number of canvases player can see that shows them
    private final IdWatcher watchedZones = new IdWatcher();
    private final ServerGamePacketListenerImpl connection;

    public HypoverseWatcher(ServerGamePacketListenerImpl connection) {
        this.connection = connection;
    }

    public ServerPlayer getPlayer() {
        return connection.getPlayer();
    }

    public void startWatchingCanvas(UUID uuid, ServerPlayer player) {
        if (this.watchedCanvases.startWatchingId(uuid)) {
            Hypoverse hypoverse = Hypoverse.fromServer(player.level().getServer());
            EldritchCanvas canvas = hypoverse.getCanvas(uuid);

            if (canvas != null) {
                ServerPlayNetworking.send(player, new StartWatchingCanvasPayload(canvas.getUuid(), canvas.getZoneId(), canvas.getDimensions()));

                UUID zoneUUID = canvas.getZoneId();
                if (this.watchedZones.startWatchingId(zoneUUID)) {
                    HypoZone zone = hypoverse.getZone(zoneUUID);
                    if (zone != null) {
                        zone.addWatcher(this);
                    }
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
                UUID zoneUUID = canvas.getZoneId();
                if (this.watchedZones.stopWatchingId(zoneUUID)) {
                    HypoZone zone = hypoverse.getZone(zoneUUID);
                    if (zone != null) {
                        zone.removeWatcher(this);
                    }
                    ServerPlayNetworking.send(player, new StopWatchingHypoZonePayload(zoneUUID));
                }
            }
        }
    }

    public boolean isWatchingZone(UUID zoneUUID) {
        return this.watchedZones.watchedIds.containsKey(zoneUUID);
    }

    public static HypoverseWatcher fromPlayer(ServerPlayer player) {
        return ((HypoverseWatcherAccess) player.connection).voidstain_hypoidol$getHypoverseWatcher();
    }
}
