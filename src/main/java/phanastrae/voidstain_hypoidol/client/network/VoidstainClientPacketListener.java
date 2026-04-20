package phanastrae.voidstain_hypoidol.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.network.StartWatchingCanvasPayload;
import phanastrae.voidstain_hypoidol.common.network.StartWatchingHypoZonePayload;
import phanastrae.voidstain_hypoidol.common.network.StopWatchingCanvasPayload;
import phanastrae.voidstain_hypoidol.common.network.StopWatchingHypoZonePayload;

public class VoidstainClientPacketListener {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(StartWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.getOrCreateZone(payload.uuid());
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
