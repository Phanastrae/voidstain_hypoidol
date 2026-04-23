package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.network.UpdateHorrorFullnessPayload;

public class HorrorHypoEntity extends BouncingHypoEntity {
    public static final String KEY_HORROR_ID = "horror_id";
    public static final String KEY_FULLNESS = "fullness";

    private int horrorId;
    private float fullness;

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
        output.putFloat(KEY_FULLNESS, this.fullness);
    }

    @Override
    public void read(CompoundTag input) {
        super.read(input);
        input.getInt(KEY_HORROR_ID).ifPresent(id -> this.horrorId = id);
        input.getInt(KEY_FULLNESS).ifPresent(fullness -> this.fullness = fullness);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        super.tick(runsNormally, onServer, hypoverse);

        if (onServer && this.random.nextInt(3) == 0) {
            for (HypoEntity entity : this.getZone().getEntitiesInArea(this.x - this.getWidth() * 0.4f, this.x + this.getWidth() * 0.4f, this.y - this.getHeight() * 0.4f, this.y + this.getHeight() * 0.4f)) {
                if (entity instanceof MorselHypoEntity morsel) {
                    this.eat(morsel);
                }
            }
        }

        if (onServer && this.random.nextInt(20) == 0) {
            this.setFullness(this.fullness * 0.99f);
        }
    }

    public void eat(MorselHypoEntity morsel) {
        morsel.setRemoved();
        this.setFullness(this.fullness + 100);
        this.playSound(SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL, 0.08f, 1.2f + this.random.nextFloat() * 0.3f);
    }

    public void setFullness(float fullness) {
        this.fullness = fullness;
        this.getZone().sendToAllWatchers(() -> new UpdateHorrorFullnessPayload(this.uuid, this.fullness));
    }

    @Override
    public float getWidth() {
        return 0.8f * this.getSizeModifier();
    }

    @Override
    public float getHeight() {
        return 0.8f * this.getSizeModifier();
    }

    public int getHorrorId() {
        return this.horrorId;
    }

    public float getFullness() {
        return this.fullness;
    }

    public float getSizeModifier() {
        return 0.125f * (float) Math.pow(2, 3 * Math.clamp(this.fullness / 4000, 0, 1));
    }
}
