package phanastrae.voidstain_hypoidol.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.network.*;

public class VoidstainClientPacketListener {

    public static void init() {
        register(StartWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.putZone(payload.uuid(), new HypoZone(payload.uuid(), payload.backgroundId()));
        }));

        register(UpdateHypoZonePayload.TYPE, ((payload, context) -> {
            Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoZone zone = hypoverse.getZone(payload.uuid());
            if (zone == null) {
                VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.uuid());
            } else {
                zone.setBackgroundId(payload.backgroundId());
            }
        }));

        register(StopWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.removeZone(payload.uuid());
        }));

        register(AddHypoEntityPayload.TYPE, ((payload, context) -> {
            Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoZone zone = hypoverse.getZone(payload.zoneUUID());
            if (zone == null) {
                VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.zoneUUID());
            } else {
                HypoEntity entity = HypoEntity.fromData(zone, payload.data());
                if (entity != null) {
                    zone.addEntity(entity);
                }
            }
        }));

        register(StartWatchingCanvasPayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.putCanvas(payload.uuid(), new EldritchCanvas(payload.uuid(), payload.zoneUUID()));
        }));

        register(StopWatchingCanvasPayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.removeCanvas(payload.uuid());
            EldritchCanvasHandler.removeCanvas(payload.uuid());
        }));
    }

    private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
