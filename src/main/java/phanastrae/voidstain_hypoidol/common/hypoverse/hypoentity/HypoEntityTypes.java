package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.VoidstainRegistries;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;

public class HypoEntityTypes {

    public static final HypoEntityType<ItemHypoEntity> ITEM = register("item", new HypoEntityType<>(ItemHypoEntity::new));
    public static final HypoEntityType<HorrorHypoEntity> HORROR = register("horror", new HypoEntityType<>(HorrorHypoEntity::new));
    public static final HypoEntityType<MorselHypoEntity> MORSEL = register("morsel", new HypoEntityType<>(MorselHypoEntity::new));
    public static final HypoEntityType<HyperGateHypoEntity> HYPERGATE = register("hypergate", new HypoEntityType<>(HyperGateHypoEntity::new));
    public static final HypoEntityType<PlayerHypoEntity> PLAYER = register("player", new HypoEntityType<>((type, zone) -> null, true));

    public static void init() {
    }

    public static <T extends HypoEntity> HypoEntityType<T> register(String id, HypoEntityType<T> type) {
        return register(VoidstainHypoidol.id(id), type);
    }

    public static <T extends HypoEntity> HypoEntityType<T> register(Identifier identifier, HypoEntityType<T> type) {
        Registry.register(VoidstainRegistries.HYPOENTITY_TYPE, identifier, type);
        return type;
    }
}
