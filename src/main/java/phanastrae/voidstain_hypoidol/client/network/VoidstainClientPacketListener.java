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
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntityTypes;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.network.*;

import java.util.UUID;

public class VoidstainClientPacketListener {

    public static void init() {
        register(StartWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.putZone(payload.uuid(), new HypoZone(payload.uuid(), payload.backgroundId(), payload.dimensions()));
        }));

        register(UpdateHypoZonePayload.TYPE, ((payload, context) -> {
            Hypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoZone zone = hypoverse.getZone(payload.uuid());
            if (zone == null) {
                VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.uuid());
            } else {
                zone.setBackgroundId(payload.backgroundId());
            }
        }));

        register(StopWatchingHypoZonePayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.removeZone(payload.uuid());
        }));

        register(AddHypoEntityPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
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
        }));

        register(AddHypoPlayerPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
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
        }));

        register(UpdateHypoEntityPositionPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoEntity entity = hypoverse.getEntity(payload.entityUUID());
            if (entity != null) {
                if (!entity.isPlayerControlled()) {
                    entity.setPos(payload.x(), payload.y());
                    entity.setVelocity(payload.vx(), payload.vy());
                }
            } else {
                VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoEntity {}", payload.entityUUID());
            }
        }));

        register(TeleportHypoEntityPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
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
        }));

        register(RemoveHypoEntityPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoEntity entity = hypoverse.removeEntity(payload.entityUUID());
            if (entity == null) {
                VoidstainHypoidol.LOGGER.warn("Tried to remove missing HypoEntity {}", payload.entityUUID());
            }
        }));

        register(UpdateHorrorFullnessPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoEntity entity = hypoverse.getEntity(payload.entityUUID());
            if (entity != null) {
                if (entity instanceof HorrorHypoEntity horror) {
                    horror.setFullness(payload.fullness());
                } else {
                    VoidstainHypoidol.LOGGER.warn("Tried to set fullness of invalid HypoEntity {}", payload.entityUUID());
                }
            } else {
                VoidstainHypoidol.LOGGER.warn("Tried to update missing HypoEntity {}", payload.entityUUID());
            }
        }));

        register(AddPortalPayload.TYPE, ((payload, context) -> {
            ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
            HypoZone zone = hypoverse.getZone(payload.zoneUUID());
            if (zone == null) {
                VoidstainHypoidol.LOGGER.warn("Received payload for missing HypoZone {}", payload.zoneUUID());
            } else {
                Portal portal = new Portal(payload.center(), payload.length(), payload.angle(), payload.id(), payload.target());
                zone.addPortal(portal);
            }
        }));

        register(StartWatchingCanvasPayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.putCanvas(payload.uuid(), new EldritchCanvas(payload.uuid(), payload.zoneUUID(), payload.dimensions()));
        }));

        register(StopWatchingCanvasPayload.TYPE, ((payload, context) -> {
            VoidstainHypoidolClient.HYPOVERSE.removeCanvas(payload.uuid());
            CanvasTextureHandler.removeCanvas(payload.uuid());
        }));
    }

    private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
