package phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.component.TypedEntityData;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityTypes;
import phanastrae.voidstain_hypoidol.common.network.AddHypoPlayerPayload;
import phanastrae.voidstain_hypoidol.common.network.HypoverseWatcher;

import java.util.UUID;

public abstract class PlayerHypoEntity extends HypoEntity {
    public static final String KEY_PLAYER_UUID = "player_uuid";

    protected final UUID playerUUID;

    public PlayerHypoEntity(HypoZone zone, UUID playerUUID) {
        super(HypoEntityTypes.PLAYER, zone);
        this.playerUUID = playerUUID;
    }

    @Override
    public float getWidth() {
        return 0.4f;
    }

    @Override
    public float getHeight() {
        return 0.4f;
    }

    @Override
    public void write(CompoundTag output) {
        super.write(output);
        output.store(KEY_PLAYER_UUID, UUIDUtil.CODEC, this.playerUUID);
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Override
    public CustomPacketPayload getAddEntityPayload(HypoverseWatcher watcher) {
        CompoundTag tag = new CompoundTag();
        this.write(tag);
        return new AddHypoPlayerPayload(this.zone.uuid, TypedEntityData.of(this.getType(), tag), watcher.getPlayer().getUUID().equals(this.playerUUID));
    }
}
