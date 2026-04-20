package phanastrae.voidstain_hypoidol.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.CanvasTexture;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasHandler;
import phanastrae.voidstain_hypoidol.client.renderer.canvas.EldritchCanvasRenderer;
import phanastrae.voidstain_hypoidol.common.entity.EldritchPaintingEntity;

public class EldritchPaintingRenderer extends EntityRenderer<EldritchPaintingEntity, EldritchPaintingRenderState> {
    private static final Identifier BACK_SPRITE_LOCATION = Identifier.withDefaultNamespace("back");
    private final TextureAtlas paintingsAtlas;

    public EldritchPaintingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.paintingsAtlas = context.getAtlas(AtlasIds.PAINTINGS);
    }

    @Override
    public void submit(EldritchPaintingRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.canvasUUID == null) {
            return;
        }
        CanvasTexture canvas = EldritchCanvasHandler.getCanvas(state.canvasUUID);
        if (!canvas.isFilled()) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - state.direction.get2DDataValue() * 90));

        TextureAtlasSprite backSprite = this.paintingsAtlas.getSprite(BACK_SPRITE_LOCATION);
        this.renderPainting(
                poseStack, submitNodeCollector,
                RenderTypes.entitySolidZOffsetForward(backSprite.atlasLocation()),
                RenderTypes.entitySolidZOffsetForward(canvas.getTextureIdentifier()),
                state.lightCoordsPerBlock,
                EldritchPaintingEntity.getWidth(), EldritchPaintingEntity.getHeight(),
                backSprite
        );

        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public EldritchPaintingRenderState createRenderState() {
        return new EldritchPaintingRenderState();
    }

    @Override
    public void extractRenderState(EldritchPaintingEntity entity, EldritchPaintingRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Direction direction = entity.getDirection();
        state.direction = direction;

        int width = EldritchPaintingEntity.getWidth();
        int height = EldritchPaintingEntity.getHeight();
        int dims = width * height;
        if (state.lightCoordsPerBlock.length != dims) {
            state.lightCoordsPerBlock = new int[dims];
        }

        float offsetX = -width / 2.0f;
        float offsetY = -height / 2.0f;
        Level level = entity.level();
        for (int segmentY = 0; segmentY < height; ++segmentY) {
            for (int segmentX = 0; segmentX < width; ++segmentX) {
                float segmentOffsetX = segmentX + offsetX + 0.5f;
                float segmentOffsetY = segmentY + offsetY + 0.5f;

                int x = entity.getBlockX();
                int y = Mth.floor(entity.getY() + segmentOffsetY);
                int z = entity.getBlockZ();

                switch (direction) {
                    case NORTH: {
                        x = Mth.floor(entity.getX() + segmentOffsetX);
                        break;
                    }
                    case WEST: {
                        z = Mth.floor(entity.getZ() - segmentOffsetX);
                        break;
                    }
                    case SOUTH: {
                        x = Mth.floor(entity.getX() - segmentOffsetX);
                        break;
                    }
                    case EAST: {
                        z = Mth.floor(entity.getZ() + segmentOffsetX);
                    }
                }

                state.lightCoordsPerBlock[segmentX + segmentY * width] = LevelRenderer.getLightCoords(level, new BlockPos(x, y, z));
            }
        }

        entity.getCanvasUUID().ifPresentOrElse(uuid -> {
            state.canvasUUID = uuid;

            EldritchCanvasRenderer.ALL_CANVAS_RENDER_STATE.activeCanvasIds.add(uuid);
        }, () -> {
            state.canvasUUID = null;
        });
    }

    private void renderPainting(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, RenderType renderType2, int[] lightCoordsMap, int width, int height, TextureAtlasSprite backSprite) {
        float edgeHalfWidth = EldritchPaintingEntity.HALF_DEPTH;

        double deltaU = 1.0 / width;
        double deltaV = 1.0 / height;
        float offsetX = -width / 2.0f;
        float offsetY = -height / 2.0f;

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            float backU0 = backSprite.getU0();
            float backU1 = backSprite.getU1();
            float backV0 = backSprite.getV0();
            float backV1 = backSprite.getV1();

            float topBottomU0 = backSprite.getU0();
            float topBottomU1 = backSprite.getU1();
            float topBottomV0 = backSprite.getV0();
            float topBottomV1 = backSprite.getV(EldritchPaintingEntity.DEPTH);

            float leftRightU0 = backSprite.getU0();
            float leftRightU1 = backSprite.getU(EldritchPaintingEntity.DEPTH);
            float leftRightV0 = backSprite.getV0();
            float leftRightV1 = backSprite.getV1();
            for (int segmentX = 0; segmentX < width; ++segmentX) {
                for (int segmentY = 0; segmentY < height; ++segmentY) {
                    float x0 = offsetX + segmentX;
                    float x1 = offsetX + segmentX + 1;
                    float y0 = offsetY + segmentY;
                    float y1 = offsetY + segmentY + 1;

                    int lightCoords = lightCoordsMap[segmentX + segmentY * width];

                    face(pose, buffer, lightCoords,
                            x0, x1, y0, y1,
                            edgeHalfWidth, edgeHalfWidth, edgeHalfWidth, edgeHalfWidth,
                            backU0, backU1, backV1, backV0,
                            0, 0, 1
                    );

                    if (segmentY == height - 1) {
                        face(pose, buffer, lightCoords,
                                x1, x0, y1, y1,
                                -edgeHalfWidth, -edgeHalfWidth, edgeHalfWidth, edgeHalfWidth,
                                topBottomU0, topBottomU1, topBottomV0, topBottomV1,
                                0, 1, 0
                        );
                    }

                    if (segmentY == 0) {
                        face(pose, buffer, lightCoords,
                                x1, x0, y0, y0,
                                edgeHalfWidth, edgeHalfWidth, -edgeHalfWidth, -edgeHalfWidth,
                                topBottomU0, topBottomU1, topBottomV0, topBottomV1,
                                0, -1, 0
                        );
                    }

                    if (segmentX == width - 1) {
                        face(pose, buffer, lightCoords,
                                x1, x1, y1, y0,
                                -edgeHalfWidth, edgeHalfWidth, edgeHalfWidth, -edgeHalfWidth,
                                leftRightU0, leftRightU1, leftRightV0, leftRightV1,
                                -1, 0, 0
                        );
                    }

                    if (segmentX == 0) {
                        face(pose, buffer, lightCoords,
                                x0, x0, y1, y0,
                                edgeHalfWidth, -edgeHalfWidth, -edgeHalfWidth, edgeHalfWidth,
                                leftRightU0, leftRightU1, leftRightV0, leftRightV1,
                                1, 0, 0
                        );
                    }
                }
            }
        });

        submitNodeCollector.submitCustomGeometry(poseStack, renderType2, (pose, buffer) -> {
            for (int segmentX = 0; segmentX < width; ++segmentX) {
                for (int segmentY = 0; segmentY < height; ++segmentY) {
                    float frontU0 = 1 - (float) (deltaU * ((segmentX + 1)));
                    float frontU1 = 1 - (float) (deltaU * segmentX);
                    // v coords are one minus what they would be otherwise, since screen texture is flipped compared to painting textures
                    float frontV0 = (float) (deltaV * (segmentY + 1));
                    float frontV1 = (float) (deltaV * segmentY);

                    float x0 = offsetX + segmentX;
                    float x1 = offsetX + segmentX + 1;
                    float y0 = offsetY + segmentY;
                    float y1 = offsetY + segmentY + 1;

                    int lightCoords = lightCoordsMap[segmentX + segmentY * width];

                    face(pose, buffer, lightCoords,
                            x1, x0, y0, y1,
                            -edgeHalfWidth, -edgeHalfWidth, -edgeHalfWidth, -edgeHalfWidth,
                            frontU0, frontU1, frontV1, frontV0,
                            0, 0, -1
                    );
                }
            }
        });
    }

    private static void face(PoseStack.Pose pose, VertexConsumer buffer, int lightCoords, float x0, float x1, float y0, float y1, float z0, float z1, float z2, float z3, float u0, float u1, float v0, float v1, int nx, int ny, int nz) {
        vertex(pose, buffer, x0, y0, z0, u0, v0, nx, ny, nz, lightCoords);
        vertex(pose, buffer, x1, y0, z1, u1, v0, nx, ny, nz, lightCoords);
        vertex(pose, buffer, x1, y1, z2, u1, v1, nx, ny, nz, lightCoords);
        vertex(pose, buffer, x0, y1, z3, u0, v1, nx, ny, nz, lightCoords);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int nx, int ny, int nz, int lightCoords) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(pose, nx, ny, nz);
    }
}