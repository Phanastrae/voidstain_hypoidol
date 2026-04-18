package phanastrae.voidstain_hypoidol.common.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

public class VoidstainEntityTypes {
    public static final EntityType<EldritchPaintingEntity> ELDRITCH_PAINTING = register(
            "eldritch_painting",
            EntityType.Builder.<EldritchPaintingEntity>of(EldritchPaintingEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
    );

    public static void init() {
    }

    public static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, VoidstainHypoidol.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
}
