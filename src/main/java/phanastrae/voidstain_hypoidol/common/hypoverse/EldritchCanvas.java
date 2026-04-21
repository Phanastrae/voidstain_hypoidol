package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.entity.EldritchPaintingEntity;

import java.util.HashSet;
import java.util.Set;
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
    private final Set<EldritchPaintingEntity> linkedPaintings = new HashSet<>();

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

    public void addLinkedPainting(EldritchPaintingEntity painting) {
        this.linkedPaintings.add(painting);
    }

    public void removeLinkedPainting(EldritchPaintingEntity painting) {
        this.linkedPaintings.remove(painting);
    }

    public void playSound(float x, float y, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        this.linkedPaintings.forEach(painting -> painting.playCanvasSound(x, y, soundEvent, source, volume, pitch));
    }
}
