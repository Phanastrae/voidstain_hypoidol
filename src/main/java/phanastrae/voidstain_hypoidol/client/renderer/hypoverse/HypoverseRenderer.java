package phanastrae.voidstain_hypoidol.client.renderer.hypoverse;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.hypoverse.ClientHypoverse;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.LocalPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.*;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.MorselHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class HypoverseRenderer {
    public static final Identifier[] BACKGROUND_IDENTIFIERS = new Identifier[]{
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_0.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_1.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/background_2.png")
    };
    public static final Identifier[] HORROR_IDENTIFIERS = new Identifier[]{
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_0.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_1.png"),
            VoidstainHypoidol.id("textures/entity/canvas/painting/horror_2.png")
    };
    public static final Identifier MORSEL_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/morsel.png");
    public static final Identifier PLAYER_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/player.png");
    public static final Identifier PORTAL_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/portal.png");
    public static final Identifier FRAME_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/frame.png");

    public static final ProjectionMatrixBuffer CANVAS_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("voidstain_canvas");

    public static HypoverseCanvasRenderer CANVAS_RENDERER = new HypoverseCanvasRenderer();
    public static HypoverseFullscreenRenderer FULLSCREEN_RENDERER = new HypoverseFullscreenRenderer();

    public static RenderType getCanvasTextureRenderType(@Nullable CanvasTexture texture, Identifier textureId) {
        return CANVAS_RENDER_TYPE.apply(texture == null ? null : texture.getCanvasId(), textureId);
    }

    private static final BiFunction<UUID, Identifier, RenderType> CANVAS_RENDER_TYPE = Util.memoize((canvasUuid, textureId) -> {
        // this should only be used if the corresponding CanvasTexture exists in the CanvasHandler, or if the input is deliberately null
        OutputTarget target = new OutputTarget("canvas_target", () -> {
            CanvasTexture canvas = CanvasTextureHandler.getCanvas(canvasUuid);
            if (canvas != null) {
                return canvas.getTarget();
            } else {
                return null;
            }
        });
        RenderSetup state = RenderSetup.builder(RenderPipelines.GUI_TEXTURED)
                .withTexture("Sampler0", textureId)
                .setOutputTarget(target)
                .createRenderSetup();

        return RenderType.create("eldritch_canvas", state);
    });

    public static void close() {
        CANVAS_PROJECTION_MATRIX_BUFFER.close();
    }

    public static void extractHypoverseData(HypoverseRenderState renderState, DeltaTracker deltaTracker) {
        ClientHypoverse hypoverse = VoidstainHypoidolClient.HYPOVERSE;
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        hypoverse.forEachZone(zone -> {
            HypoZoneRenderState zoneRenderState = new HypoZoneRenderState();
            zoneRenderState.backgroundId = zone.getBackgroundId();
            zoneRenderState.dimensions = zone.getDimensions();

            for (HypoEntity entity : zone.entities) {
                HypoEntityRenderState entityRenderState = null;
                if (entity instanceof HorrorHypoEntity horror) {
                    HorrorRenderState state = new HorrorRenderState();
                    state.horrorId = horror.getHorrorId();
                    state.sizeModifier = horror.getSizeModifier();

                    entityRenderState = state;
                } else if (entity instanceof MorselHypoEntity morsel) {
                    entityRenderState = new MorselRenderState();
                } else if (entity instanceof PlayerHypoEntity player) {
                    entityRenderState = new PlayerRenderState();
                }

                if (entityRenderState != null) {
                    entityRenderState.x = Mth.lerp(partialTick, entity.ox, entity.x);
                    entityRenderState.y = Mth.lerp(partialTick, entity.oy, entity.y);

                    zoneRenderState.entities.add(entityRenderState);
                }
            }
            for (Portal portal : zone.portals.values()) {
                PortalRenderState portalRenderState = new PortalRenderState();
                portalRenderState.start = portal.getStartPos();
                portalRenderState.end = portal.getEndPos();
                portalRenderState.normal = portal.getNormal();

                zoneRenderState.portals.add(portalRenderState);
            }

            renderState.zones.put(zone.uuid, zoneRenderState);
        });

        LocalPlayerHypoEntity player = hypoverse.hypoPlayer;
        if (player == null) {
            renderState.mainZoneUUID = null;
            renderState.playerX = 0;
            renderState.playerY = 0;
        } else {
            renderState.mainZoneUUID = hypoverse.hypoPlayer.getZone().uuid;
            renderState.playerX = Mth.lerp(partialTick, player.ox, player.x);
            renderState.playerY = Mth.lerp(partialTick, player.oy, player.y);
        }
    }

    public static void tryRenderZone(@Nullable CanvasTexture texture, UUID zoneUUID, HypoverseRenderState hypoverseRenderState) {
        if (hypoverseRenderState.zones.containsKey(zoneUUID)) {
            HypoZoneRenderState zoneRenderState = hypoverseRenderState.zones.get(zoneUUID);
            renderZone(texture, zoneRenderState);
        }
    }

    public static void renderZone(@Nullable CanvasTexture texture, HypoZoneRenderState zoneRenderState) {
        HypoZone.Dimensions dimensions = zoneRenderState.dimensions;

        drawWithTexture(texture, BACKGROUND_IDENTIFIERS[zoneRenderState.backgroundId], (builder) -> {
            drawQuad(builder, dimensions.minX, dimensions.maxX, dimensions.minY, dimensions.maxY);
        });

        for (HypoEntityRenderState entityRenderState : zoneRenderState.entities) {
            float x = entityRenderState.x - dimensions.minX;
            float y = entityRenderState.y - dimensions.minY;
            switch (entityRenderState) {
                case HorrorRenderState horrorRenderState -> {
                    float halfWidth = horrorRenderState.sizeModifier / 2f;
                    float halfHeight = horrorRenderState.sizeModifier / 2f;
                    drawWithTexture(texture, HORROR_IDENTIFIERS[horrorRenderState.horrorId], (builder) -> {
                        drawQuad(builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    });
                }
                case MorselRenderState morselRenderState -> {
                    float halfWidth = 0.25f;
                    float halfHeight = 0.25f;
                    drawWithTexture(texture, MORSEL_IDENTIFIER, (builder) -> {
                        drawQuad(builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    });
                }
                case PlayerRenderState playerRenderState -> {
                    float halfWidth = 0.2f;
                    float halfHeight = 0.2f;
                    drawWithTexture(texture, PLAYER_IDENTIFIER, (builder) -> {
                        drawQuad(builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    });
                }
                default -> {
                }
            }
        }

        for (PortalRenderState portalRenderState : zoneRenderState.portals) {
            float startX = portalRenderState.start.x - dimensions.minX;
            float startY = portalRenderState.start.y - dimensions.minX;
            float endX = portalRenderState.end.x - dimensions.minX;
            float endY = portalRenderState.end.y - dimensions.minX;

            float dx = 0.03125f * portalRenderState.normal.x;
            float dy = 0.03125f * portalRenderState.normal.y;

            drawWithTexture(texture, PORTAL_IDENTIFIER, (builder) -> {
                builder.addVertex(startX - dx, startY - dy, 0.0f).setUv(0, 1).setColor(255, 255, 255, 255);
                builder.addVertex(startX + dx, startY + dy, 0.0f).setUv(1, 1).setColor(255, 255, 255, 255);
                builder.addVertex(endX + dx, endY + dy, 0.0f).setUv(1, 0).setColor(255, 255, 255, 255);
                builder.addVertex(endX - dx, endY - dy, 0.0f).setUv(0, 0).setColor(255, 255, 255, 255);
            });
        }
    }

    public static void drawWithTexture(@Nullable CanvasTexture texture, Identifier textureId, Consumer<BufferBuilder> runnable) {
        RenderType type = getCanvasTextureRenderType(texture, textureId);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        runnable.accept(builder);
        MeshData mesh = builder.build();
        if (mesh != null) {
            type.draw(mesh);
        }
    }

    public static void drawQuad(BufferBuilder builder, float x0, float x1, float y0, float y1) {
        drawQuad(builder, x0, x1, y0, y1, 0, 1, 0, 1);
    }

    public static void drawQuad(BufferBuilder builder, float x0, float x1, float y0, float y1, float u0, float u1, float v0, float v1) {
        builder.addVertex(x0, y0, 0.0f).setUv(u0, v1).setColor(255, 255, 255, 255);
        builder.addVertex(x1, y0, 0.0f).setUv(u1, v1).setColor(255, 255, 255, 255);
        builder.addVertex(x1, y1, 0.0f).setUv(u1, v0).setColor(255, 255, 255, 255);
        builder.addVertex(x0, y1, 0.0f).setUv(u0, v0).setColor(255, 255, 255, 255);
    }
}
