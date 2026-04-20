package phanastrae.voidstain_hypoidol.mixin.client;

import net.minecraft.client.multiplayer.CacheSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin
        extends Level
        implements BlockAndTintGetter,
        CacheSlot.Cleaner<ClientLevel>,
        HypoverseAccess {
    private ClientLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Override
    public Hypoverse voidstain_hypoidol$getHypoverse() {
        return VoidstainHypoidolClient.HYPOVERSE;
    }
}
