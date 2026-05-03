package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.ServerPlayerHypoEntity;

public class MorselHypoEntity extends BouncingHypoEntity {
    public static final String KEY_LIFE = "life";

    private int life;

    public MorselHypoEntity(HypoEntityType<? extends MorselHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    public MorselHypoEntity(HypoZone zone) {
        super(HypoEntityTypes.MORSEL, zone);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        if (onServer) {
            if (this.life <= 0) {
                this.setRemoved();
            }
        }
        this.life--;

        if (!this.isRemoved()) {
            super.tick(runsNormally, onServer, hypoverse);
        }
    }

    @Override
    public void write(CompoundTag output) {
        super.write(output);
        output.putInt(KEY_LIFE, this.life);
    }

    @Override
    public void read(CompoundTag input) {
        super.read(input);
        input.getInt(KEY_LIFE).ifPresent(i -> this.life = i);
    }

    @Override
    public float getWidth() {
        return 0.2f;
    }

    @Override
    public float getHeight() {
        return 0.2f;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getLife() {
        return this.life;
    }
}
