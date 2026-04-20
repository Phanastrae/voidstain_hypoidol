package phanastrae.voidstain_hypoidol.common.hypoverse;

import java.util.UUID;

public class EldritchCanvas {

    private final UUID uuid;
    private final UUID zoneUUID;

    public EldritchCanvas(UUID uuid, UUID zoneUUID) {
        this.uuid = uuid;
        this.zoneUUID = zoneUUID;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public UUID getZoneId() {
        return this.zoneUUID;
    }
}
