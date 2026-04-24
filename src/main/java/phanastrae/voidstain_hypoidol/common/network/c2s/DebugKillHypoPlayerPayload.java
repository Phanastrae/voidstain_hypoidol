package phanastrae.voidstain_hypoidol.common.network.c2s;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public class DebugKillHypoPlayerPayload implements CustomPacketPayload {
    public static final DebugKillHypoPlayerPayload INSTANCE = new DebugKillHypoPlayerPayload();

    public static final Type<DebugKillHypoPlayerPayload> TYPE = new Type<>(VoidstainHypoidol.id("debug_kill_hypo_player"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DebugKillHypoPlayerPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private DebugKillHypoPlayerPayload() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
