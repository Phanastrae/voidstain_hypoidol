package phanastrae.voidstain_hypoidol.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ColoredNameItem extends Item {

    private final int color;

    public ColoredNameItem(Properties properties, int color) {
        super(properties);
        this.color = color;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return super.getName(itemStack).copy().withColor(this.color);
    }
}
