package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class VoidstainPayloads {

    public static void init() {
        registerClientboundPlay(StartWatchingHypoZonePayload.TYPE, StartWatchingHypoZonePayload.STREAM_CODEC);
        registerClientboundPlay(UpdateHypoZonePayload.TYPE, UpdateHypoZonePayload.STREAM_CODEC);
        registerClientboundPlay(StopWatchingHypoZonePayload.TYPE, StopWatchingHypoZonePayload.STREAM_CODEC);

        registerClientboundPlay(AddHypoEntityPayload.TYPE, AddHypoEntityPayload.STREAM_CODEC);
        registerClientboundPlay(AddHypoPlayerPayload.TYPE, AddHypoPlayerPayload.STREAM_CODEC);
        registerClientboundPlay(UpdateHypoEntityPositionPayload.TYPE, UpdateHypoEntityPositionPayload.STREAM_CODEC);
        registerClientboundPlay(TeleportHypoEntityPayload.TYPE, TeleportHypoEntityPayload.STREAM_CODEC);
        registerClientboundPlay(RemoveHypoEntityPayload.TYPE, RemoveHypoEntityPayload.STREAM_CODEC);
        registerClientboundPlay(UpdateHorrorFullnessPayload.TYPE, UpdateHorrorFullnessPayload.STREAM_CODEC);

        registerClientboundPlay(AddPortalPayload.TYPE, AddPortalPayload.STREAM_CODEC);

        registerClientboundPlay(StartWatchingCanvasPayload.TYPE, StartWatchingCanvasPayload.STREAM_CODEC);
        registerClientboundPlay(StopWatchingCanvasPayload.TYPE, StopWatchingCanvasPayload.STREAM_CODEC);


        registerServerboundPlay(DebugKillHypoPlayerPayload.TYPE, DebugKillHypoPlayerPayload.STREAM_CODEC);
    }

    private static <T extends CustomPacketPayload> void registerClientboundPlay(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
    }

    private static <T extends CustomPacketPayload> void registerServerboundPlay(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
    }
}
