package phanastrae.voidstain_hypoidol.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.hypoverse.ClientHypoverse;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.ClientPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.LocalPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.RemotePlayerHypoEntity;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.CanvasTextureHandler;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.EldritchCanvas;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityTypes;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.network.s2c.*;

import java.util.UUID;

public class VoidstainClientPacketListener {

    public static void init() {
        register(StartWatchingHypoZonePayload.TYPE, VoidstainClientPacketListener::startWatchingHypoZone);
        register(UpdateHypoZonePayload.TYPE, VoidstainClientPacketListener::updateHypoZone);
        register(StopWatchingHypoZonePayload.TYPE, VoidstainClientPacketListener::stopWatchingHypoZone);

        register(AddHypoEntityPayload.TYPE, VoidstainClientPacketListener::addHypoEntity);
        register(AddHypoPlayerPayload.TYPE, VoidstainClientPacketListener::addHypoPlayer);
        register(UpdateHypoEntityPositionPayload.TYPE, VoidstainClientPacketListener::updateHypoEntityPosition);
        register(TeleportHypoEntityPayload.TYPE, VoidstainClientPacketListener::teleportHypoEntity);
        register(RemoveHypoEntityPayload.TYPE, VoidstainClientPacketListener::removeHypoEntity);
        register(UpdateHorrorFullnessPayload.TYPE, VoidstainClientPacketListener::updateHorrorFullness);

        register(AddPortalPayload.TYPE, VoidstainClientPacketListener::addPortal);

        register(StartWatchingCanvasPayload.TYPE, VoidstainClientPacketListener::startWatchingCanvas);
        register(StopWatchingCanvasPayload.TYPE, VoidstainClientPacketListener::stopWatchingCanvas);
    }

    private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }

    public static void startWatchingHypoZone(StartWatchingHypoZonePayload payload, ClientPlayNetworking.Context context) {
        getHypoverse().putZone(payload.uuid(), new HypoZone(payload.uuid(), payload.backgroundId(), payload.dimensions()));
    }

    public static void updateHypoZone(UpdateHypoZonePayload payload, ClientPlayNetworking.Context context) {
        HypoZone zone = getHypoverse().getZone(payload.uuid());
        if (zone == null) {
            VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.uuid());
        } else {
            zone.setBackgroundId(payload.backgroundId());
        }
    }

    public static void stopWatchingHypoZone(StopWatchingHypoZonePayload payload, ClientPlayNetworking.Context context) {
        getHypoverse().removeZone(payload.uuid());
    }

    public static void addHypoEntity(AddHypoEntityPayload payload, ClientPlayNetworking.Context context) {
        ClientHypoverse hypoverse = getHypoverse();
        HypoZone zone = hypoverse.getZone(payload.zoneUUID());
        if (zone == null) {
            VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.zoneUUID());
        } else {
            if (payload.data().type().equals(HypoEntityTypes.PLAYER)) {
                VoidstainHypoidol.LOGGER.warn("Tried to add a HypoPlayer with a regular AddHypoEntityPayload?");
            } else {
                HypoEntity entity = HypoEntity.fromData(zone, payload.data());
                if (entity != null) {
                    hypoverse.addEntity(entity);
                }
            }
        }
    }

    public static void addHypoPlayer(AddHypoPlayerPayload payload, ClientPlayNetworking.Context context) {
        ClientHypoverse hypoverse = getHypoverse();
        HypoZone zone = hypoverse.getZone(payload.zoneUUID());
        if (zone == null) {
            VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.zoneUUID());
        } else {
            if (payload.data().type().equals(HypoEntityTypes.PLAYER)) {
                CompoundTag tag = payload.data().copyTagWithoutId();
                UUID playerUUID = tag.read(PlayerHypoEntity.KEY_PLAYER_UUID, UUIDUtil.CODEC).orElse(null);
                if (playerUUID != null) {
                    ClientPlayerHypoEntity entity = payload.isLocal() ? new LocalPlayerHypoEntity(zone, playerUUID) : new RemotePlayerHypoEntity(zone, playerUUID);
                    entity.read(tag);
                    hypoverse.addEntity(entity);
                } else {
                    VoidstainHypoidol.LOGGER.warn("Tried to add HypoPlayer with no player UUID?");
                }
            } else {
                VoidstainHypoidol.LOGGER.warn("Tried to add a non-player HypoEntity with an AddHypoPlayerPayload?");
            }
        }
    }

    public static void updateHypoEntityPosition(UpdateHypoEntityPositionPayload payload, ClientPlayNetworking.Context context) {
        HypoEntity entity = getHypoverse().getEntity(payload.entityUUID());
        if (entity != null) {
            if (!entity.isPlayerControlled()) {
                entity.setPos(payload.x(), payload.y());
                entity.setVelocity(payload.vx(), payload.vy());
            }
        } else {
            VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoEntity {}", payload.entityUUID());
        }
    }

    public static void teleportHypoEntity(TeleportHypoEntityPayload payload, ClientPlayNetworking.Context context) {
        ClientHypoverse hypoverse = getHypoverse();
        HypoEntity entity = hypoverse.getEntity(payload.entityUUID());
        if (entity != null) {
            HypoZone zone = hypoverse.getZone(payload.zoneUUID());
            if (zone != null) {
                entity.setPos(payload.x(), payload.y());
                entity.setOldPos(payload.ox(), payload.oy());
                entity.setVelocity(payload.vx(), payload.vy());
                entity.setZone(zone);
            } else {
                hypoverse.removeEntity(entity.getUuid());
                VoidstainHypoidol.LOGGER.warn("Removed HypoEntity {} that was teleported to missing HypoZone {}, this should have been a RemoveEntityPayload instead.", entity.getUuid(), payload.zoneUUID());
            }
        } else {
            VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoEntity {}", payload.entityUUID());
        }
    }

    public static void removeHypoEntity(RemoveHypoEntityPayload payload, ClientPlayNetworking.Context context) {
        HypoEntity entity = getHypoverse().removeEntity(payload.entityUUID());
        if (entity == null) {
            VoidstainHypoidol.LOGGER.warn("Tried to remove missing HypoEntity {}", payload.entityUUID());
        }
    }

    public static void updateHorrorFullness(UpdateHorrorFullnessPayload payload, ClientPlayNetworking.Context context) {
        HypoEntity entity = getHypoverse().getEntity(payload.entityUUID());
        if (entity != null) {
            if (entity instanceof HorrorHypoEntity horror) {
                horror.setFullness(payload.fullness());
            } else {
                VoidstainHypoidol.LOGGER.warn("Tried to set fullness of invalid HypoEntity {}", payload.entityUUID());
            }
        } else {
            VoidstainHypoidol.LOGGER.warn("Tried to update missing HypoEntity {}", payload.entityUUID());
        }
    }

    public static void addPortal(AddPortalPayload payload, ClientPlayNetworking.Context context) {
        HypoZone zone = getHypoverse().getZone(payload.zoneUUID());
        if (zone == null) {
            VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.zoneUUID());
        } else {
            Portal portal = new Portal(payload.center(), payload.length(), payload.angle(), payload.id(), payload.target());
            zone.addPortal(portal);
        }
    }

    public static void startWatchingCanvas(StartWatchingCanvasPayload payload, ClientPlayNetworking.Context context) {
        VoidstainHypoidolClient.HYPOVERSE.putCanvas(payload.uuid(), new EldritchCanvas(payload.uuid(), payload.zoneUUID(), payload.dimensions()));
    }

    public static void stopWatchingCanvas(StopWatchingCanvasPayload payload, ClientPlayNetworking.Context context) {
        VoidstainHypoidolClient.HYPOVERSE.removeCanvas(payload.uuid());
        CanvasTextureHandler.removeCanvas(payload.uuid());
    }

    public static ClientHypoverse getHypoverse() {
        return VoidstainHypoidolClient.HYPOVERSE;
    }
}
