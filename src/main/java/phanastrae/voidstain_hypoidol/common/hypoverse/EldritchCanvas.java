package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;

import java.util.UUID;

public class EldritchCanvas extends SavedData {
    public static final Codec<EldritchCanvas> CODEC = RecordCodecBuilder.create(i -> i.group(
                    UUIDUtil.CODEC.fieldOf("canvas_uuid").forGetter(EldritchCanvas::getUuid),
                    UUIDUtil.CODEC.fieldOf("zone_uuid").forGetter(EldritchCanvas::getZoneId)
            ).apply(i, EldritchCanvas::new)
    );

    public static SavedDataType<EldritchCanvas> type(Hypoverse hypoverse, UUID canvasUUID) {
        return new SavedDataType<>(VoidstainHypoidol.id("hypoverse/canvas/" + canvasUUID), () -> {
            return new EldritchCanvas(canvasUUID, Mth.createInsecureUUID(hypoverse.random));
        }, CODEC, null);
    }

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
