package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class VoidstainServerPacketListener {

    public static void init() {
        register(DebugKillHypoPlayerPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            HypoverseWatcher watcher = HypoverseWatcher.fromPlayer(player);
            if (watcher.hasHypoPlayer()) {
                watcher.killHypoPlayer();
            }
        });
    }


    private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ServerPlayNetworking.PlayPayloadHandler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
