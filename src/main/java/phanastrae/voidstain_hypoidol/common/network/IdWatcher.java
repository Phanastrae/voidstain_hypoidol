package phanastrae.voidstain_hypoidol.common.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IdWatcher {

    public final Map<UUID, AtomicInteger> watchedIds = new HashMap<>();

    public boolean startWatchingId(UUID uuid) {
        AtomicInteger viewCount;
        if (!this.watchedIds.containsKey(uuid)) {
            viewCount = new AtomicInteger(1);
            this.watchedIds.put(uuid, viewCount);
            return true;
        } else {
            this.watchedIds.get(uuid).incrementAndGet();
        }
        return false;
    }

    public boolean stopWatchingId(UUID uuid) {
        if (this.watchedIds.containsKey(uuid)) {
            AtomicInteger viewCount = this.watchedIds.get(uuid);
            int count = viewCount.decrementAndGet();
            if (count <= 0) {
                this.watchedIds.remove(uuid, viewCount);
                return true;
            }
        }
        return false;
    }
}
