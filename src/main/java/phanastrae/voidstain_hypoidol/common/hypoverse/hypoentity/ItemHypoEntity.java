package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.ServerPlayerHypoEntity;

public class ItemHypoEntity extends HypoEntity {
    public static final String KEY_ITEM = "item";
    public static final String KEY_LIFE = "life";

    private int life;
    private ItemStack item = ItemStack.EMPTY;

    public ItemHypoEntity(HypoEntityType<? extends ItemHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    public ItemHypoEntity(HypoZone zone) {
        super(HypoEntityTypes.ITEM, zone);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        if (onServer) {
            if (this.life <= 0) {
                this.setRemoved();
            } else {
                this.zone.getEntitiesInArea(
                        this.x - this.getWidth() / 2,
                        this.x + this.getWidth() / 2,
                        this.y - this.getWidth() / 2,
                        this.y + this.getWidth() / 2
                ).forEach(e -> {
                    if (e instanceof ServerPlayerHypoEntity hypoPlayer) {
                        ServerPlayer serverPlayer = hypoPlayer.getWatcher().getPlayer();
                        giveToPlayer(serverPlayer, hypoverse);
                    }
                });
            }
        }
        this.life--;

        if (!this.isRemoved()) {
            super.tick(runsNormally, onServer, hypoverse);
        }
    }

    public void giveToPlayer(Player player, Hypoverse hypoverse) {
        ItemStack thisStack = this.item;
        Item item = thisStack.getItem();
        int originalCount = thisStack.getCount();
        if (player.getInventory().add(thisStack)) {
            this.playSound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4f, 1.5f);
            player.awardStat(Stats.ITEM_PICKED_UP.get(item), originalCount - thisStack.count());
            if (thisStack.isEmpty()) {
                this.setRemoved();
            }
        }
    }

    @Override
    public void write(CompoundTag output) {
        super.write(output);
        output.putInt(KEY_LIFE, this.life);
        if (!this.item.isEmpty()) {
            output.store(KEY_ITEM, ItemStack.CODEC, this.item);
        }
    }

    @Override
    public void read(CompoundTag input) {
        super.read(input);
        input.getInt(KEY_LIFE).ifPresent(i -> this.life = i);
        input.read(KEY_ITEM, ItemStack.CODEC).ifPresent(stack -> this.item = stack);
        if (this.item.isEmpty()) {
            this.setRemoved();
        }
    }

    @Override
    public float getWidth() {
        return 0.14f;
    }

    @Override
    public float getHeight() {
        return 0.14f;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getLife() {
        return this.life;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}