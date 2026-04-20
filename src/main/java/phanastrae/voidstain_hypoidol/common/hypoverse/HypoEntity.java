package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.util.RandomSource;

public class HypoEntity {

    private final RandomSource random = RandomSource.create();
    private final HypoLevel hypoLevel;
    public float ox;
    public float oy;
    public float x;
    public float y;
    public int horrorId;

    public HypoEntity(HypoLevel hypoLevel) {
        this.hypoLevel = hypoLevel;
        this.horrorId = random.nextInt(3);
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
}
