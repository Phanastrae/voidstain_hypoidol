package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
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
                    UUIDUtil.CODEC.fieldOf("zone_uuid").forGetter(EldritchCanvas::getZoneId),
                    Dimensions.CODEC.fieldOf("dimensions").forGetter(EldritchCanvas::getDimensions)
            ).apply(i, EldritchCanvas::new)
    );

    public static SavedDataType<EldritchCanvas> type(Hypoverse hypoverse, UUID canvasUUID, Dimensions dimensions) {
        return new SavedDataType<>(VoidstainHypoidol.id("hypoverse/canvas/" + canvasUUID), () -> {
            return new EldritchCanvas(canvasUUID, canvasUUID, dimensions);
        }, CODEC, null);
    }

    private final Set<EldritchPaintingEntity> linkedPaintings = new HashSet<>();
    private final UUID uuid;
    private final UUID zoneUUID;
    private final Dimensions dimensions;

    public EldritchCanvas(UUID uuid, UUID zoneUUID, Dimensions dimensions) {
        this.uuid = uuid;
        this.zoneUUID = zoneUUID;
        this.dimensions = dimensions;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public UUID getZoneId() {
        return this.zoneUUID;
    }

    public Dimensions getDimensions() {
        return this.dimensions;
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

    public static class Dimensions {
        public static final Codec<Dimensions> CODEC = RecordCodecBuilder.create(i -> i.group(
                        ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(d -> d.width),
                        ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(d -> d.height)
                ).apply(i, Dimensions::new)
        );
        public static final StreamCodec<FriendlyByteBuf, Dimensions> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                d -> d.width,
                ByteBufCodecs.INT,
                d -> d.height,
                Dimensions::new
        );

        public final int width;
        public final int height;

        public Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
