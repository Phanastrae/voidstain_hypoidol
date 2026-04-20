package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class VoidstainPayloads {

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(StartWatchingHypoZonePayload.TYPE, StartWatchingHypoZonePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StopWatchingHypoZonePayload.TYPE, StopWatchingHypoZonePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StartWatchingCanvasPayload.TYPE, StartWatchingCanvasPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StopWatchingCanvasPayload.TYPE, StopWatchingCanvasPayload.STREAM_CODEC);
    }
}
