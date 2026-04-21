package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.nbt.CompoundTag;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

public class HorrorHypoEntity extends HypoEntity {
    public static final String KEY_HORROR_ID = "horror_id";

    private int horrorId;

    public HorrorHypoEntity(HypoEntityType<? extends HorrorHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    public HorrorHypoEntity(HypoZone zone, int horrorId) {
        super(HypoEntityTypes.HORROR, zone);
        this.horrorId = horrorId;
    }

    @Override
    public void write(CompoundTag output) {
        super.write(output);
        output.putInt(KEY_HORROR_ID, this.horrorId);
    }

    @Override
    public void read(CompoundTag input) {
        super.read(input);
        input.getInt(KEY_HORROR_ID).ifPresent(id -> this.horrorId = id);
    }

    public int getHorrorId() {
        return this.horrorId;
    }
}
