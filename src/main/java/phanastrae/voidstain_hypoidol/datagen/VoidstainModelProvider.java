package phanastrae.voidstain_hypoidol.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import phanastrae.voidstain_hypoidol.common.item.VoidstainItems;

public class VoidstainModelProvider extends FabricModelProvider {
    public VoidstainModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(VoidstainItems.ELDRITCH_PAINTING, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(VoidstainItems.LOVE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(VoidstainItems.UNCERTAINTY, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(VoidstainItems.HATRED, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(VoidstainItems.FEAR, ModelTemplates.FLAT_ITEM);
    }

    @Override
    public String getName() {
        return "VoidstainHypoidolModelProvider";
    }
}
