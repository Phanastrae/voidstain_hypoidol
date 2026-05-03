package phanastrae.voidstain_hypoidol.client.renderer.hypoverse;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec2;
import org.joml.Quaternionf;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.hypoverse.ClientHypoverse;
import phanastrae.voidstain_hypoidol.client.hypoverse.hypoentity.player.LocalPlayerHypoEntity;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion.CameraView;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.*;
import phanastrae.voidstain_hypoidol.common.VoidstainHypoidol;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HorrorHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.HypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.ItemHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.MorselHypoEntity;
import phanastrae.voidstain_hypoidol.common.hypoverse.hypoentity.player.PlayerHypoEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public static final Identifier ITEM_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/item.png");
    public static final Identifier MORSEL_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/morsel.png");
    public static final Identifier PLAYER_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/player.png");
    public static final Identifier PORTAL_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/portal.png");
    public static final Identifier FRAME_IDENTIFIER = VoidstainHypoidol.id("textures/entity/canvas/painting/frame.png");

    public static final ProjectionMatrixBuffer CANVAS_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("voidstain_canvas");

    public static HypoverseCanvasRenderer CANVAS_RENDERER = new HypoverseCanvasRenderer();
    public static HypoverseFullscreenRenderer FULLSCREEN_RENDERER = new HypoverseFullscreenRenderer();

    private static final Function<Identifier, RenderType> NEAREST_RENDER_TYPE = Util.memoize(createFunc(FilterMode.NEAREST));
    private static final Function<Identifier, RenderType> LINEAR_RENDER_TYPE = Util.memoize(createFunc(FilterMode.LINEAR));

    public static final RenderPipeline DRAW_CANVAS_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withLocation(VoidstainHypoidol.id("pipeline/draw_canvas"))
            .build()
    );

    private static Function<Identifier, RenderType> createFunc(FilterMode filterMode) {
        return (textureId) -> {
            RenderSetup state = RenderSetup.builder(DRAW_CANVAS_PIPELINE)
                    .withTexture("Sampler0", textureId, () -> RenderSystem.getSamplerCache().getClampToEdge(filterMode))
                    .createRenderSetup();

            return RenderType.create("eldritch_canvas", state);
        };
    }

    public static final RenderPipeline WRITE_DEPTH_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position")
            .withFragmentShader("core/position")
            .withColorTargetState(new ColorTargetState(Optional.empty(), 0))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLES)
            .withCull(false)
            .withLocation(VoidstainHypoidol.id("pipeline/write_depth"))
            .build()
    );

    public static final RenderType WRITE_DEPTH_TYPE = RenderType.create("write_depth",
            RenderSetup.builder(WRITE_DEPTH_PIPELINE).createRenderSetup()
    );

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
                } else if (entity instanceof ItemHypoEntity item) {
                    ItemRenderState itemRenderState = new ItemRenderState();
                    itemRenderState.life = item.getLife();
                    entityRenderState = itemRenderState;
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
                portalRenderState.center = portal.getCenter();
                portalRenderState.normal = portal.getNormal();
                portalRenderState.length = portal.getLength();
                portalRenderState.angle = portal.getAngle();

                zoneRenderState.portals.add(portalRenderState);
            }

            renderState.zones.put(zone.uuid, zoneRenderState);
        });

        LocalPlayerHypoEntity player = hypoverse.hypoPlayer;
        if (player == null) {
            renderState.cameraView = null;
        } else {
            renderState.cameraView = CameraView.create(
                    new Vec2(Mth.lerp(partialTick, player.ox, player.x), Mth.lerp(partialTick, player.oy, player.y)),
                    new Vec2(player.x, player.y),
                    player.getZone(),
                    hypoverse,
                    2
            );
        }
    }

    public static void tryRenderZone(UUID zoneUUID, HypoverseRenderState hypoverseRenderState) {
        tryRenderZone(new PoseStack(), zoneUUID, hypoverseRenderState);
    }

    public static void tryRenderZone(PoseStack poseStack, UUID zoneUUID, HypoverseRenderState hypoverseRenderState) {
        if (hypoverseRenderState.zones.containsKey(zoneUUID)) {
            HypoZoneRenderState zoneRenderState = hypoverseRenderState.zones.get(zoneUUID);
            renderZone(poseStack, zoneRenderState);
        }
    }

    public static void renderZone(PoseStack poseStack, HypoZoneRenderState zoneRenderState) {
        PoseStack.Pose pose = poseStack.last();

        HypoZone.Dimensions dimensions = zoneRenderState.dimensions;

        drawWithTexture(BACKGROUND_IDENTIFIERS[zoneRenderState.backgroundId], (builder) -> {
            drawQuad(pose, builder, dimensions.minX, dimensions.maxX, dimensions.minY, dimensions.maxY);
        }, true);

        for (HypoEntityRenderState entityRenderState : zoneRenderState.entities) {
            float x = entityRenderState.x;
            float y = entityRenderState.y;
            switch (entityRenderState) {
                case HorrorRenderState horrorRenderState -> {
                    float halfWidth = horrorRenderState.sizeModifier * 0.8f;
                    float halfHeight = horrorRenderState.sizeModifier * 0.8f;
                    drawWithTexture(HORROR_IDENTIFIERS[horrorRenderState.horrorId], (builder) -> {
                        drawQuad(pose, builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    }, true);
                }
                case MorselRenderState morselRenderState -> {
                    float halfWidth = 0.25f;
                    float halfHeight = 0.25f;
                    drawWithTexture(MORSEL_IDENTIFIER, (builder) -> {
                        drawQuad(pose, builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    }, true);
                }
                case ItemRenderState itemRenderState -> {
                    float sizeModifier = Math.clamp(itemRenderState.life, 0, 200) / 200f;
                    sizeModifier = 1 - (1 - sizeModifier) * (1 - sizeModifier);
                    float halfWidth = 0.07f * sizeModifier;
                    float halfHeight = 0.07f * sizeModifier;
                    drawWithTexture(ITEM_IDENTIFIER, (builder) -> {
                        drawQuad(pose, builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    }, true);
                }
                case PlayerRenderState playerRenderState -> {
                    float halfWidth = 0.2f;
                    float halfHeight = 0.2f;
                    drawWithTexture(PLAYER_IDENTIFIER, (builder) -> {
                        drawQuad(pose, builder, x - halfWidth, x + halfWidth, y - halfHeight, y + halfHeight);
                    }, true);
                }
                default -> {
                }
            }
        }

        for (PortalRenderState portalRenderState : zoneRenderState.portals) {
            poseStack.pushPose();
            poseStack.translate(portalRenderState.center.x, portalRenderState.center.y, 0);
            poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(portalRenderState.angle - 90))); // offset by 90 to orient the texture properly

            float halfLength = portalRenderState.length / 2;
            float halfWidth = halfLength / 16;

            PoseStack.Pose pose2 = poseStack.last();
            drawWithTexture(PORTAL_IDENTIFIER, builder -> drawQuad(pose2, builder, -halfWidth, halfWidth, -halfLength, halfLength));

            poseStack.popPose();
        }
    }

    public static void drawWithTexture(Identifier textureId, Consumer<BufferBuilder> runnable) {
        drawWithTexture(textureId, runnable, false);
    }

    public static void drawWithTexture(Identifier textureId, Consumer<BufferBuilder> runnable, boolean linear) {
        RenderType type = linear ? LINEAR_RENDER_TYPE.apply(textureId) : NEAREST_RENDER_TYPE.apply(textureId);
        drawWithRenderType(type, runnable);
    }

    public static void drawWithRenderType(RenderType type, Consumer<BufferBuilder> runnable) {
        BufferBuilder builder = Tesselator.getInstance().begin(type.mode(), type.format());
        runnable.accept(builder);
        MeshData mesh = builder.build();
        if (mesh != null) {
            type.draw(mesh);
        }
    }

    public static void drawQuad(PoseStack.Pose pose, BufferBuilder builder, float x0, float x1, float y0, float y1) {
        drawQuad(pose, builder, x0, x1, y0, y1, 0, 1, 0, 1);
    }

    public static void drawQuad(PoseStack.Pose pose, BufferBuilder builder, float x0, float x1, float y0, float y1, float u0, float u1, float v0, float v1) {
        builder.addVertex(pose, x0, y0, 0.0f).setUv(u0, v1).setColor(255, 255, 255, 255);
        builder.addVertex(pose, x1, y0, 0.0f).setUv(u1, v1).setColor(255, 255, 255, 255);
        builder.addVertex(pose, x1, y1, 0.0f).setUv(u1, v0).setColor(255, 255, 255, 255);
        builder.addVertex(pose, x0, y1, 0.0f).setUv(u0, v0).setColor(255, 255, 255, 255);
    }
}
