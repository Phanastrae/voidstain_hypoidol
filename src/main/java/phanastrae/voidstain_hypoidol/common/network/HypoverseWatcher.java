package phanastrae.voidstain_hypoidol.common.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.duck.HypoverseWatcherAccess;
import phanastrae.voidstain_hypoidol.common.duck.PlayerDuck;
import phanastrae.voidstain_hypoidol.common.hypoverse.*;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.ServerPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.common.network.s2c.SetEntityInHypoversePayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.StartWatchingCanvasPayload;
import phanastrae.voidstain_hypoidol.common.network.s2c.StopWatchingCanvasPayload;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class HypoverseWatcher {

    // map of canvas ids to count of paintings player can see that shows them
    private final IdWatcher watchedCanvases = new IdWatcher();
    // map of level ids to number of canvases player can see that shows them
    private final IdWatcher watchedZones = new IdWatcher();
    private final ServerGamePacketListenerImpl connection;

    private final Set<UUID> directlyWatchedZones = new HashSet<>();

    @Nullable
    private ServerPlayerHypoEntity hypoPlayer;

    public HypoverseWatcher(ServerGamePacketListenerImpl connection) {
        this.connection = connection;
    }

    public ServerPlayer getPlayer() {
        return connection.getPlayer();
    }

    public void startWatchingCanvas(UUID uuid, ServerPlayer player) {
        if (this.watchedCanvases.startWatchingId(uuid)) {
            Hypoverse hypoverse = Hypoverse.fromServer(player.level().getServer());
            EldritchCanvas canvas = hypoverse.getCanvas(uuid);

            if (canvas != null) {
                ServerPlayNetworking.send(player, new StartWatchingCanvasPayload(canvas.getUuid(), canvas.getZoneId(), canvas.getDimensions()));

                UUID zoneUUID = canvas.getZoneId();
                this.startWatchingZone(hypoverse, zoneUUID);
            }
        }
    }

    public void stopWatchingCanvas(UUID uuid, ServerPlayer player) {
        if (this.watchedCanvases.stopWatchingId(uuid)) {
            ServerPlayNetworking.send(player, new StopWatchingCanvasPayload(uuid));

            Hypoverse hypoverse = Hypoverse.fromServer(player.level().getServer());
            EldritchCanvas canvas = hypoverse.getCanvas(uuid);
            if (canvas != null) {
                UUID zoneUUID = canvas.getZoneId();
                this.stopWatchingZone(hypoverse, zoneUUID);
            }
        }
    }

    public void startWatchingZone(Hypoverse hypoverse, UUID zoneUUID) {
        if (this.watchedZones.startWatchingId(zoneUUID)) {
            HypoZone zone = hypoverse.getZone(zoneUUID);
            if (zone != null) {
                zone.addWatcher(this);
            }
        }
    }

    public void stopWatchingZone(Hypoverse hypoverse, UUID zoneUUID) {
        if (this.watchedZones.stopWatchingId(zoneUUID)) {
            HypoZone zone = hypoverse.getZone(zoneUUID);
            if (zone != null) {
                zone.removeWatcher(this);
            }
        }
    }

    public void updateDirectlyWatchedZones() {
        ServerHypoverse hypoverse = Hypoverse.fromServer(this.getPlayer().level().getServer());
        Set<UUID> watchedZones = new HashSet<>();

        HypoZone playerZone = this.hypoPlayer != null ? this.hypoPlayer.getZone() : null;
        if (playerZone != null) {
            watchedZones.add(playerZone.uuid);
        }

        // expand watched zones out through portals
        Set<UUID> newlyWatchedZones = new HashSet<>(watchedZones);
        Set<UUID> targetZones = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            for (UUID uuid : newlyWatchedZones) {
                HypoZone zone = hypoverse.getZone(uuid);
                if (zone != null) {
                    for (Portal portal : zone.portals.values()) {
                        UUID targetUUID = portal.getTargetId().zoneUUID;
                        targetZones.add(targetUUID);
                    }
                }
            }

            newlyWatchedZones.clear();
            for (UUID uuid : targetZones) {
                if (!watchedZones.contains(uuid)) {
                    watchedZones.add(uuid);
                    newlyWatchedZones.add(uuid);
                }
            }
            targetZones.clear();
        }

        // update watched zones
        for (UUID uuid : watchedZones) {
            if (!this.directlyWatchedZones.contains(uuid)) {
                hypoverse.startWatchingZone(uuid, null);
                this.startWatchingZone(hypoverse, uuid);
            }
        }

        for (UUID uuid : this.directlyWatchedZones) {
            if (!watchedZones.contains(uuid)) {
                this.stopWatchingZone(hypoverse, uuid);
                hypoverse.stopWatchingZone(uuid);
            }
        }

        this.directlyWatchedZones.clear();
        this.directlyWatchedZones.addAll(watchedZones);
    }

    public PlayerHypoEntity createHypoPlayer(Hypoverse hypoverse, HypoZone zone, float x, float y, Consumer<ServerPlayerHypoEntity> modifyPlayer) {
        if (this.hypoPlayer != null) {
            this.hypoPlayer.setRemoved();
        }

        ServerPlayerHypoEntity playerHypoEntity = new ServerPlayerHypoEntity(zone, this, this.connection.player.getUUID());
        playerHypoEntity.setPos(x, y);
        modifyPlayer.accept(playerHypoEntity);
        hypoverse.addEntity(playerHypoEntity);
        this.hypoPlayer = playerHypoEntity;
        this.setPlayerInHypoverse(true);
        return this.hypoPlayer;
    }

    public void onHypoPlayerRemoval() {
        this.hypoPlayer = null;
        this.setPlayerInHypoverse(false);
    }

    public boolean hasHypoPlayer() {
        return this.hypoPlayer != null;
    }

    @Nullable
    public ServerPlayerHypoEntity getHypoPlayer() {
        return this.hypoPlayer;
    }

    public void killHypoPlayer() {
        this.getPlayer().setPortalCooldown(50);
        Hypoverse hypoverse = Hypoverse.fromLevel(this.getPlayer().level());
        if (this.hypoPlayer != null && hypoverse != null) {
            this.hypoPlayer.setRemoved();
            this.hypoPlayer = null;
            this.setPlayerInHypoverse(false);
        }
    }

    public boolean isWatchingZone(UUID zoneUUID) {
        return this.watchedZones.watchedIds.containsKey(zoneUUID);
    }

    public static HypoverseWatcher fromPlayer(ServerPlayer player) {
        return ((HypoverseWatcherAccess) player.connection).voidstain_hypoidol$getHypoverseWatcher();
    }

    public void setPlayerInHypoverse(boolean value) {
        ServerPlayer player = this.getPlayer();
        if (setPlayerInHypoverse(player, value)) {
            SetEntityInHypoversePayload payload = new SetEntityInHypoversePayload(player.getId(), value);

            for (ServerPlayer serverPlayer : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(serverPlayer, payload);
            }
            ServerPlayNetworking.send(player, payload);

            this.updateDirectlyWatchedZones();
        }
    }

    public static boolean setPlayerInHypoverse(Player player, boolean value) {
        PlayerDuck p = (PlayerDuck) player;
        if (p.voidstain_hypoidol$isInHypoverse() != value) {
            p.voidstain_hypoidol$setInHypoverse(value);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPlayerInHypoverse(Player player) {
        return ((PlayerDuck) player).voidstain_hypoidol$isInHypoverse();
    }
}
