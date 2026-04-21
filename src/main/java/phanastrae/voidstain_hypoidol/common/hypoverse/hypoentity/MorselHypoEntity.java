package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity;

import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;

public class MorselHypoEntity extends HypoEntity {

    public MorselHypoEntity(HypoEntityType<? extends MorselHypoEntity> type, HypoZone zone) {
        super(type, zone);
    }

    public MorselHypoEntity(HypoZone zone) {
        super(HypoEntityTypes.MORSEL, zone);
    }
}
