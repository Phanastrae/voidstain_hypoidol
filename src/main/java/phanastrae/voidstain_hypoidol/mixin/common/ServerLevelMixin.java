package phanastrae.voidstain_hypoidol.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntityGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseAccess;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin
        extends Level
        implements WorldGenLevel,
        ServerEntityGetter,
        HypoverseAccess {
    @Shadow
    @Final
    private MinecraftServer server;

    private ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Override
    public Hypoverse voidstain_hypoidol$getHypoverse() {
        return Hypoverse.fromServer(this.server);
    }
}
