package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityType;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;
import phanastrae.voidstain_hypoidol.common.network.s2c.AddPortalPayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.RemovePortalPayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.StartWatchingHypoZonePayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.UpdateHypoZonePayload;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class HypoZone extends SavedData {
    public static final Codec<HypoZone> CODEC = RecordCodecBuilder.create(i -> i.group(
                    UUIDUtil.CODEC.fieldOf("zone_uuid").forGetter(HypoZone::getUuid),
                    Codec.INT.fieldOf("background_id").forGetter(HypoZone::getBackgroundId),
                    HypoEntity.CODEC.listOf().optionalFieldOf("entity_data", List.of()).forGetter(HypoZone::getEntityData),
                    Dimensions.CODEC.fieldOf("dimensions").forGetter(zone -> zone.dimensions),
                    Portal.CODEC.listOf().optionalFieldOf("portals", List.of()).forGetter(zone -> zone.portals.values().stream().toList())
            ).apply(i, HypoZone::new)
    );

    public static SavedDataType<HypoZone> type(Hypoverse hypoverse, UUID zoneUUID, Dimensions dimensions) {
        return new SavedDataType<>(VoidstainHypoidol.id("hypoverse/zone/" + zoneUUID), () -> {
            return new HypoZone(zoneUUID, RandomSource.create().nextInt(3), dimensions);
        }, CODEC, null);
    }

    private final Set<HypoverseWatcher> watchers = new HashSet<>();
    private final List<HypoverseWatcher> newWatchers = new ArrayList<>();
    private final Set<EldritchCanvas> linkedCanvases = new HashSet<>();
    private boolean isClientDirty;

    private final RandomSource random = RandomSource.create();

    public final UUID uuid;
    private int backgroundId;
    private final Dimensions dimensions;
    public final List<HypoEntity> entities;
    public final Map<Integer, Portal> portals;

    public HypoZone(UUID uuid, int backgroundId, Dimensions dimensions) {
        this(uuid, backgroundId, List.of(), dimensions, List.of());
    }

    public HypoZone(UUID uuid, int backgroundId, List<TypedEntityData<HypoEntityType<?>>> entityData, Dimensions dimensions, List<Portal> portals) {
        this.uuid = uuid;
        this.backgroundId = backgroundId;
        this.dimensions = dimensions;

        this.entities = new ArrayList<>();
        entityData.forEach(data -> {
            HypoEntity entity = HypoEntity.fromData(this, data);
            if (entity != null) {
                this.entities.add(entity);
            }
        });
        this.portals = new HashMap<>();
        portals.forEach(p -> this.portals.put(p.getId(), p));
    }

    public List<TypedEntityData<HypoEntityType<?>>> getEntityData() {
        if (this.entities.isEmpty()) {
            return List.of();
        } else {
            List<TypedEntityData<HypoEntityType<?>>> data = new ArrayList<>();
            for (HypoEntity entity : this.entities) {
                if (entity.getType().canSave()) {
                    data.add(entity.getData());
                }
            }
            return data;
        }
    }

    public void tick(boolean runsNormally, boolean onServer, Hypoverse hypoverse) {
        if (onServer) {
            this.setDirty();
            this.entities.stream().filter(HypoEntity::isRemoved).toList().forEach(e -> hypoverse.removeEntity(e.getUuid()));

            if (!this.newWatchers.isEmpty()) {
                this.updateNewWatchers();
                this.watchers.addAll(this.newWatchers);
                this.newWatchers.clear();
            }
        }
    }

    public Collection<HypoEntity> getEntitiesInArea(float minX, float maxX, float minY, float maxY) {
        List<HypoEntity> es = new ArrayList<>();
        for (HypoEntity entity : this.entities) {
            if (entity.isRemoved()) {
                continue;
            }

            float eminX = entity.x - entity.getWidth() / 2;
            float emaxX = entity.x + entity.getHeight() / 2;
            float eminY = entity.y - entity.getWidth() / 2;
            float emaxY = entity.y + entity.getHeight() / 2;

            if (eminX <= maxX && minX <= emaxX && eminY <= maxY && minY <= emaxY) {
                es.add(entity);
            }
        }
        return es;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
        this.markClientDirty();
        this.setDirty();
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public Dimensions getDimensions() {
        return this.dimensions;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void markClientDirty() {
        this.isClientDirty = true;
    }

    public void markNotClientDirty() {
        this.isClientDirty = false;
    }

    public boolean isClientDirty() {
        return this.isClientDirty;
    }

    public void addWatcher(HypoverseWatcher watcher) {
        this.newWatchers.add(watcher);
    }

    public void removeWatcher(HypoverseWatcher watcher) {
        this.watchers.remove(watcher);
        this.newWatchers.remove(watcher);
    }

    public void addLinkedCanvas(EldritchCanvas canvas) {
        this.linkedCanvases.add(canvas);
    }

    public void removeLinkedCanvas(EldritchCanvas canvas) {
        this.linkedCanvases.remove(canvas);
    }

    public void addPortal(Portal portal) {
        this.portals.put(portal.getId(), portal);
        this.sendToAllWatchers(() -> getAddPortalPayload(portal));
        this.setDirty();
    }

    @Nullable
    public Portal removePortal(int id) {
        Portal portal = this.portals.remove(id);
        if (portal != null) {
            this.sendToAllWatchers(() -> new RemovePortalPayload(this.uuid, id));
            this.setDirty();
        }
        return portal;
    }

    public void updateNewWatchers() {
        sendToAll(this.newWatchers, () -> new StartWatchingHypoZonePayload(this.uuid, this.getBackgroundId(), this.dimensions), w -> true);
        for (HypoEntity entity : this.entities) {
            sendToAll(this.newWatchers, entity::getAddEntityPayload, w -> true);
        }
        for (Portal portal : this.portals.values()) {
            sendToAll(this.newWatchers, () -> getAddPortalPayload(portal), w -> true);
        }
    }

    private AddPortalPayload getAddPortalPayload(Portal portal) {
        return new AddPortalPayload(this.uuid, portal.getCenter(), portal.getLength(), portal.getAngle(), portal.getId(), portal.getTargetId());
    }

    public void sendUpdates() {
        this.sendToAllWatchers(() -> new UpdateHypoZonePayload(this.uuid, this.backgroundId));
        this.markNotClientDirty();
    }

    public void sendToAllWatchersNotAlsoWatching(Supplier<CustomPacketPayload> payloadSupplier, UUID otherZoneUUID) {
        sendToAll(this.watchers, payloadSupplier, w -> !w.isWatchingZone(otherZoneUUID));
    }

    public void sendToAllWatchersAlsoWatching(Supplier<CustomPacketPayload> payloadSupplier, UUID otherZoneUUID) {
        sendToAll(this.watchers, payloadSupplier, w -> w.isWatchingZone(otherZoneUUID));
    }

    public void sendToAllWatchers(Supplier<CustomPacketPayload> payloadSupplier) {
        sendToAll(this.watchers, payloadSupplier, w -> true);
    }

    public static void sendToAll(Collection<HypoverseWatcher> sendTo, Supplier<CustomPacketPayload> payloadSupplier, Predicate<HypoverseWatcher> predicate) {
        if (!sendTo.isEmpty()) {
            CustomPacketPayload payload = payloadSupplier.get();
            sendTo.forEach(watcher -> {
                if (predicate.test(watcher)) {
                    ServerPlayNetworking.send(watcher.getPlayer(), payload);
                }
            });
        }
    }

    public void sendToAllWatchersNotAlsoWatching(Function<HypoverseWatcher, CustomPacketPayload> function, UUID otherZoneUUID) {
        sendToAll(this.watchers, function, w -> !w.isWatchingZone(otherZoneUUID));
    }

    public void sendToAllWatchersAlsoWatching(Function<HypoverseWatcher, CustomPacketPayload> function, UUID otherZoneUUID) {
        sendToAll(this.watchers, function, w -> w.isWatchingZone(otherZoneUUID));
    }

    public void sendToAllWatchers(Function<HypoverseWatcher, CustomPacketPayload> function) {
        sendToAllWatchers(function, w -> true);
    }

    public void sendToAllWatchers(Function<HypoverseWatcher, CustomPacketPayload> function, Predicate<HypoverseWatcher> predicate) {
        sendToAll(this.watchers, function, predicate);
    }

    public static void sendToAll(Collection<HypoverseWatcher> sendTo, Function<HypoverseWatcher, CustomPacketPayload> function, Predicate<HypoverseWatcher> predicate) {
        if (!sendTo.isEmpty()) {
            sendTo.forEach(watcher -> {
                if (predicate.test(watcher)) {
                    ServerPlayNetworking.send(watcher.getPlayer(), function.apply(watcher));
                }
            });
        }
    }

    public void playSound(float x, float y, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        this.linkedCanvases.forEach(canvas -> canvas.playSound(x, y, soundEvent, source, volume, pitch));
    }

    public int getEmptyPortalId(int alsoAvoid) {
        for (int i = 0; i < 100; i++) {
            int candidate = this.random.nextInt();
            if (candidate != alsoAvoid && !this.portals.containsKey(candidate)) {
                return candidate;
            }
        }
        VoidstainHypoidol.LOGGER.error("Failed to generate an unused portal id for HypoZone {}???", this.uuid);
        return this.random.nextInt();
    }

    public static class Dimensions {
        public static final Codec<Dimensions> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codec.INT.fieldOf("xmin").forGetter(d -> d.minX),
                        Codec.INT.fieldOf("ymin").forGetter(d -> d.minY),
                        ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(d -> d.width),
                        ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(d -> d.height)
                ).apply(i, Dimensions::new)
        );
        public static final StreamCodec<FriendlyByteBuf, Dimensions> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                d -> d.minX,
                ByteBufCodecs.INT,
                d -> d.minY,
                ByteBufCodecs.INT,
                d -> d.width,
                ByteBufCodecs.INT,
                d -> d.height,
                Dimensions::new
        );

        public final int minX;
        public final int minY;

        public final int width;
        public final int height;

        public final int maxX;
        public final int maxY;

        public Dimensions(int width, int height) {
            this(0, 0, width, height);
        }

        public Dimensions(int minX, int minY, int width, int height) {
            this.minX = minX;
            this.minY = minY;
            this.width = width;
            this.height = height;
            this.maxX = minX + width;
            this.maxY = minY + height;
        }
    }
}
