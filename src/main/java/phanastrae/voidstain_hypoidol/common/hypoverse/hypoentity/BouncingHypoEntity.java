package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;

public abstract class BouncingHypoEntity extends HypoEntity {

    public BouncingHypoEntity(HypoEntityType<? extends BouncingHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    @Override
    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        super.tick(runsNormally, onServer, hypoverse);

        if (onServer && this.random.nextInt(40) == 0) {
            this.vx += (this.random.nextFloat() - 0.5f) * 0.1f;
            this.vy += (this.random.nextFloat() - 0.5f) * 0.1f;
            this.needsSync = true;
        }
    }
}
