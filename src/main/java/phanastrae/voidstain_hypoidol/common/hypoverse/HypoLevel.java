package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class HypoLevel {

    private final RandomSource random = RandomSource.create();
    private final Hypoverse hypoverse;
    public final String id;
    public List<HypoEntity> entities = new ArrayList<>();
    public int backgroundId;

    public HypoLevel(Hypoverse hypoverse, String id) {
        this.hypoverse = hypoverse;
        this.id = id;
        this.backgroundId = this.random.nextInt(3);
        for (int i = 0; i < random.nextIntBetweenInclusive(1, 3); i++) {
            this.entities.add(new HypoEntity(this));
        }
    }

    public void tick(boolean runsNormally) {
        this.entities.forEach(e -> e.tick(runsNormally));
    }
}
