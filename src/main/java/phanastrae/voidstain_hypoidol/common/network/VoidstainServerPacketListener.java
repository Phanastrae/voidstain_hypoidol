package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import phanastrae.voidstain_hypoidol.common.network.c2s.DebugKillHypoPlayerPayload;

public class VoidstainServerPacketListener {

    public static void init() {
        register(DebugKillHypoPlayerPayload.TYPE, VoidstainServerPacketListener::debugKillHypoPlayer);
    }

    private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ServerPlayNetworking.PlayPayloadHandler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type, handler);
    }

    public static void debugKillHypoPlayer(DebugKillHypoPlayerPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(player);
        if (watcher.hasHypoPlayer()) {
            watcher.killHypoPlayer();
        }
    }
}
