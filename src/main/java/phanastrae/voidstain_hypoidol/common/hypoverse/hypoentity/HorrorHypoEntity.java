package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.nbt.CompoundTag;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

public class HorrorHypoEntity extends HypoEntity {

    private int horrorId;

    public HorrorHypoEntity(HypoEntityType<? extends HorrorHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    public HorrorHypoEntity(HypoZone zone, int horrorId) {
        super(HypoEntityTypes.HORROR, zone);
        this.horrorId = horrorId;
    }

    @Override
    public void write(CompoundTag compoundTag) {
        super.write(compoundTag);
        compoundTag.putInt("horror_id", this.horrorId);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
        compoundTag.getInt("horror_id").ifPresent(id -> this.horrorId = id);
    }

    public int getHorrorId() {
        return this.horrorId;
    }
}
