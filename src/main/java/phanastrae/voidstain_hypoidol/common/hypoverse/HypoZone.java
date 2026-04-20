package phanastrae.voidstain_hypoidol.common.hypoverse;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HypoZone {

    private final RandomSource random = RandomSource.create();
    private final Hypoverse hypoverse;
    public final UUID uuid;
    public List<HypoEntity> entities = new ArrayList<>();
    public int backgroundId;

    public HypoZone(Hypoverse hypoverse, UUID uuid) {
        this.hypoverse = hypoverse;
        this.uuid = uuid;
        this.random.setSeed(uuid.hashCode());
        this.backgroundId = this.random.nextInt(3);
        for (int i = 0; i < random.nextIntBetweenInclusive(1, 3); i++) {
            this.entities.add(new HypoEntity(this));
        }
    }

    public void tick(boolean runsNormally) {
        this.entities.forEach(e -> e.tick(runsNormally));
    }
}
