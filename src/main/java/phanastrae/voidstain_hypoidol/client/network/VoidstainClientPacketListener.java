package phanastrae.voidstain_hypoidol.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.network.*;

public class VoidstainClientPacketListener {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(StartWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            hypoverse.putZone(payload.uuid(), new HypoZone(hypoverse, payload.uuid(), payload.backgroundId()));
        }));
        ClientPlayNetworking.registerGlobalReceiver(UpdateHypoZonePayload.TYPE, ((payload, context) -> {
            Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoZone zone = hypoverse.getZone(payload.uuid());
            if (zone == null) {
                VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone " + payload.uuid());
            } else {
                zone.setBackgroundId(payload.backgroundId());
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(StopWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.removeZone(payload.uuid());
        }));
        ClientPlayNetworking.registerGlobalReceiver(StartWatchingCanvasPayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.putCanvas(payload.uuid(), new EldritchCanvas(payload.uuid(), payload.zoneUUID()));
        }));
        ClientPlayNetworking.registerGlobalReceiver(StopWatchingCanvasPayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.removeCanvas(payload.uuid());
            EldritchCanvasHandler.removeCanvas(payload.uuid());
        }));
    }
}
