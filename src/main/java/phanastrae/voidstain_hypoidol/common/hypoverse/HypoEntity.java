package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.util.RandomSource;

public class HypoEntity {

    private final RandomSource random = RandomSource.create();
    private final HypoZone zone;
    public float ox;
    public float oy;
    public float x;
    public float y;
    public int horrorId;

    public HypoEntity(HypoZone zone, int horrorId) {
        this.zone = zone;
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
}
