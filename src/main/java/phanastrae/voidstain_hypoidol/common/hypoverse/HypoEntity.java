package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public class HypoEntity {
    public static final Codec<HypoEntity> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.INT.fieldOf("horror_id").forGetter(HypoEntity::getHorrorId)
            ).apply(i, HypoEntity::new)
    );

    private final RandomSource random = RandomSource.create();
    public float ox;
    public float oy;
    public float x;
    public float y;
    public int horrorId;

    public HypoEntity(int horrorId) {
        this.horrorId = horrorId;
    }

    public void tick(boolean runsNormally) {
        if (runsNormally) {
            this.ox = x;
            this.oy = y;

            this.x += (random.nextFloat() - 0.5f) * 0.125f;
            this.y += (random.nextFloat() - 0.5f) * 0.125f;

            this.x = Math.clamp(this.x, -1.5f, 1.5f);
            this.y = Math.clamp(this.y, -1.5f, 1.5f);
        }
    }

    public int getHorrorId() {
        return this.horrorId;
    }
}
