package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityType;
import phanastrae.voidstain_hypoidol.common.network.AddHypoEntityPayload;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;
import phanastrae.voidstain_hypoidol.common.network.StartWatchingHypoZonePayload;
import phanastrae.voidstain_hypoidol.common.network.UpdateHypoZonePayload;

import java.util.*;
import java.util.function.Supplier;

public class HypoZone extends SavedData {
    public static final Codec<HypoZone> CODEC = RecordCodecBuilder.create(i -> i.group(
                    UUIDUtil.CODEC.fieldOf("zone_uuid").forGetter(HypoZone::getUuid),
                    Codec.INT.fieldOf("background_id").forGetter(HypoZone::getBackgroundId),
                    HypoEntity.CODEC.listOf().optionalFieldOf("entity_data", List.of()).forGetter(HypoZone::getEntityData)
            ).apply(i, HypoZone::new)
    );

    public static SavedDataType<HypoZone> type(Hypoverse hypoverse, UUID zoneUUID) {
        return new SavedDataType<>(VoidstainHypoidol.id("hypoverse/zone/" + zoneUUID), () -> {
            return new HypoZone(zoneUUID, RandomSource.create().nextInt(3));
        }, CODEC, null);
    }

    private final RandomSource random = RandomSource.create();
    private final Set<HypoverseWatcher> watchers = new HashSet<>();

    public final UUID uuid;
    private int backgroundId;
    public final List<HypoEntity> entities;

    private boolean isClientDirty;

    public HypoZone(UUID uuid, int backgroundId) {
        this(uuid, backgroundId, List.of());
    }

    public HypoZone(UUID uuid, int backgroundId, List<TypedEntityData<HypoEntityType<?>>> entityData) {
        this.uuid = uuid;
        this.random.setSeed(uuid.hashCode());
        this.backgroundId = backgroundId;
        this.entities = new ArrayList<>();
        entityData.forEach(data -> {
            HypoEntity entity = HypoEntity.fromData(this, data);
            if (entity != null) {
                this.entities.add(entity);
            }
        });
    }

    public List<TypedEntityData<HypoEntityType<?>>> getEntityData() {
        if (this.entities.isEmpty()) {
            return List.of();
        } else {
            List<TypedEntityData<HypoEntityType<?>>> data = new ArrayList<>();
            for (HypoEntity entity : this.entities) {
                data.add(entity.getData());
            }
            return data;
        }
    }

    public void tick(boolean runsNormally, boolean onServer) {
        this.entities.forEach(e -> e.tick(runsNormally, onServer));

        if (onServer) {
            this.setDirty();

            if (!this.watchers.isEmpty()) {
                this.entities.forEach(e -> e.sendChanges((payload) -> this.sendToAllWatchers(() -> payload)));
            }
        }
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
        this.markClientDirty();
        this.setDirty();
    }

    public int getBackgroundId() {
        return backgroundId;
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
        this.watchers.add(watcher);
    }

    public void removeWatcher(HypoverseWatcher watcher) {
        this.watchers.remove(watcher);
    }

    public void addEntity(HypoEntity entity) {
        this.entities.add(entity);
        this.sendToAllWatchers(() -> getPayload(entity));
        this.setDirty();
    }

    public void updateNewWatcher(HypoverseWatcher watcher) {
        ServerPlayNetworking.send(watcher.getPlayer(), new StartWatchingHypoZonePayload(this.uuid, this.getBackgroundId()));
        for (HypoEntity entity : this.entities) {
            CustomPacketPayload payload = getPayload(entity);
            ServerPlayNetworking.send(watcher.getPlayer(), payload);
        }
    }

    private AddHypoEntityPayload getPayload(HypoEntity entity) {
        CompoundTag tag = new CompoundTag();
        entity.write(tag);
        return new AddHypoEntityPayload(this.uuid, TypedEntityData.of(entity.getType(), tag));
    }

    public void sendUpdates() {
        this.sendToAllWatchers(() -> new UpdateHypoZonePayload(this.uuid, this.backgroundId));
        this.markNotClientDirty();
    }

    public void sendToAllWatchers(Supplier<CustomPacketPayload> payloadSupplier) {
        if (!this.watchers.isEmpty()) {
            CustomPacketPayload payload = payloadSupplier.get();
            this.watchers.forEach(watcher -> {
                ServerPlayNetworking.send(watcher.getPlayer(), payload);
            });
        }
    }
}
