package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.ServerHypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.ServerPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.network.c2s.DebugKillHypoPlayerPayload;
import phanastrae.voidstain_hypoidol.common.network.c2s.MoveHypoPlayerPayload;
import phanastrae.voidstain_hypoidol.common.network.c2s.TeleportHypoPlayerPayload;

public class VoidstainServerPacketListener {

    public static void init() {
        register(MoveHypoPlayerPayload.TYPE, VoidstainServerPacketListener::moveHypoPlayer);
        register(TeleportHypoPlayerPayload.TYPE, VoidstainServerPacketListener::teleportHypoPlayer);
        register(DebugKillHypoPlayerPayload.TYPE, VoidstainServerPacketListener::debugKillHypoPlayer);
    }

    private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ServerPlayNetworking.PlayPayloadHandler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type, handler);
    }

    public static void moveHypoPlayer(MoveHypoPlayerPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(player);
        ServerPlayerHypoEntity hypoPlayer = watcher.getHypoPlayer();
        if (hypoPlayer != null) {
            float x = payload.x();
            float y = payload.y();
            float vx = payload.vx();
            float vy = payload.vy();
            float angle = payload.angle();
            float vAngle = payload.vAngle();
            if (Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(vx) && Float.isFinite(vy) && Float.isFinite(angle) && Float.isFinite(vAngle)) {
                hypoPlayer.setPos(x, y);
                hypoPlayer.setVelocity(vx, vy);
                hypoPlayer.setAngle(angle);
                hypoPlayer.setAngleVelocity(vAngle);
            }
        }
    }

    public static void teleportHypoPlayer(TeleportHypoPlayerPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(player);
        ServerPlayerHypoEntity hypoPlayer = watcher.getHypoPlayer();
        if (hypoPlayer != null) {
            float x = payload.x();
            float y = payload.y();
            float vx = payload.vx();
            float vy = payload.vy();
            float angle = payload.angle();
            float vAngle = payload.vAngle();
            if (Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(vx) && Float.isFinite(vy) && Float.isFinite(angle) && Float.isFinite(vAngle)) {
                HypoZone zone = getHypoverse(context).getZone(payload.zoneUUID());
                if (zone != null) {
                    hypoPlayer.setPos(x, y);
                    hypoPlayer.setVelocity(vx, vy);
                    hypoPlayer.setOldPos(x, y);
                    hypoPlayer.setAngle(angle);
                    hypoPlayer.setOldAngle(angle);
                    hypoPlayer.setAngleVelocity(vAngle);
                    hypoPlayer.setZone(zone);
                }
            }
        }
    }

    public static void debugKillHypoPlayer(DebugKillHypoPlayerPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(player);
        if (watcher.hasHypoPlayer()) {
            watcher.killHypoPlayer();
        }
    }

    public static ServerHypoverse getHypoverse(ServerPlayNetworking.Context context) {
        return ServerHypoverse.fromServer(context.server());
    }
}
