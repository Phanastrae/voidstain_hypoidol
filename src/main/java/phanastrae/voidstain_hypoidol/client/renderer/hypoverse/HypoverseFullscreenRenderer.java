package phanastrae.voidstain_hypoidol.client.renderer.hypoverse;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import phanastrae.voidstain_hypoidol.client.VoidstainHypoidolClient;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion.CameraView;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion.LineSegment;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion.SegmentedLine;
import phanastrae.voidstain_hypoidol.client.renderer.hypoverse.state.HypoverseRenderState;

import java.util.UUID;

public class HypoverseFullscreenRenderer {
    private final HypoverseRenderState hypoverseRenderState = new HypoverseRenderState();
    private boolean shouldRender;
    private final CanvasTexture paintingTexture = new CanvasTexture(Mth.createInsecureUUID(RandomSource.create()), 8, 8);

    public void close() {
        this.paintingTexture.close();
    }

    public void decideShouldRender() {
        this.shouldRender = VoidstainHypoidolClient.HYPOVERSE.hypoPlayer != null;
    }

    public boolean shouldRender() {
        return this.shouldRender;
    }

    public void extract(DeltaTracker deltaTracker) {
        if (this.shouldRender) {
            HypoverseRenderer.extractHypoverseData(this.hypoverseRenderState, deltaTracker);
        }
    }

    public void render() {
        if (this.shouldRender) {
            renderFullscreen(this.hypoverseRenderState);
            this.hypoverseRenderState.reset();
        }
    }

    public void renderFullscreen(HypoverseRenderState renderState) {
        if (renderState.cameraView != null) {
            RenderSystem.backupProjectionMatrix();
            Projection projection = new Projection();
            projection.setupOrtho(-1.0f, 1.0f, 1, 1, false);
            RenderSystem.setProjectionMatrix(HypoverseRenderer.CANVAS_PROJECTION_MATRIX_BUFFER.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.identity();

            Window window = Minecraft.getInstance().getWindow();

            // translate player to screen center
            modelViewStack.translate(0.5f, 0.5f, 0);

            // scale screen
            float scale = 0.6f;
            modelViewStack.scale(scale * window.getHeight() / window.getWidth(), scale, 1);

            // draw background
            HypoverseRenderer.drawWithRenderType(RenderTypes.endPortal(), builder -> {
                // TODO scale this properly on weird aspect ratios
                // TODO does this ever interact weridly with fog?
                HypoverseRenderer.drawQuad(new PoseStack.Pose(), builder, -100f, 100f, -100f, 100f);
            });

            // rotate camera
            modelViewStack.rotate(-renderState.cameraAngle, 0, 0, 1);
            // translate bottom left corner to player pos
            modelViewStack.translate(-renderState.cameraView.cameraPos.x, -renderState.cameraView.cameraPos.y, 0);
            GpuTexture depthTexture = Minecraft.getInstance().getMainRenderTarget().getDepthTexture();
            if (depthTexture != null) {
                clearDepthTexture(depthTexture, 1.0);

                renderCameraView(renderState.cameraView, new PoseStack(), renderState, renderState.cameraView.zoneUUID, depthTexture);
                clearDepthTexture(depthTexture, 1.0);
            }

            modelViewStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }


    public void renderCameraView(CameraView cameraView, PoseStack poseStack, HypoverseRenderState hypoverseRenderState, UUID zoneUUID, GpuTexture depthTexture) {
        if (!cameraView.occlusion.lines.isEmpty()) {
            // block drawing to portal interiors
            poseStack.pushPose();
            poseStack.translate(
                    cameraView.cameraPos.x,
                    cameraView.cameraPos.y,
                    0
            );
            PoseStack.Pose pose = poseStack.last();
            HypoverseRenderer.drawWithRenderType(HypoverseRenderer.WRITE_DEPTH_TYPE, builder -> {
                for (SegmentedLine line : cameraView.occlusion.lines) {
                    for (LineSegment segment : line.segments) {
                        drawSegmentArea(builder, pose, segment, 1);
                    }
                }
            });
            poseStack.popPose();
        }

        HypoverseRenderer.tryRenderZone(poseStack, zoneUUID, hypoverseRenderState, this.paintingTexture);

        if (!cameraView.childViews.isEmpty()) {
            // draw portal interiors
            for (CameraView subView : cameraView.childViews) {
                if (subView.occlusionLine != null) {
                    SegmentedLine occlusionLine = subView.occlusionLine;
                    // setup draw area
                    clearDepthTexture(depthTexture, 0.0);

                    poseStack.pushPose();
                    poseStack.translate(
                            cameraView.cameraPos.x,
                            cameraView.cameraPos.y,
                            0
                    );
                    PoseStack.Pose pose = poseStack.last();
                    HypoverseRenderer.drawWithRenderType(HypoverseRenderer.WRITE_DEPTH_TYPE, builder -> {
                        for (LineSegment segment : occlusionLine.segments) {
                            drawSegmentArea(builder, pose, segment, -1);
                        }
                    });
                    poseStack.popPose();

                    // draw zones
                    poseStack.pushPose();
                    poseStack.translate(
                            occlusionLine.lineData.center.x,
                            occlusionLine.lineData.center.y,
                            0
                    );
                    poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(occlusionLine.lineData.angle - occlusionLine.lineData.targetAngle)));
                    poseStack.translate(
                            -occlusionLine.lineData.targetCenter.x,
                            -occlusionLine.lineData.targetCenter.y,
                            0
                    );

                    renderCameraView(subView, poseStack, hypoverseRenderState, occlusionLine.lineData.targetZone, depthTexture);

                    poseStack.popPose();
                }
            }
        }
    }

    public static void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(depthTexture, clearDepth);
    }

    public static Vec2 fromPolar(float radius, double angle) {
        return new Vec2(radius * (float) Math.cos(angle), radius * (float) Math.sin(angle));
    }

    public static void drawSegmentArea(BufferBuilder builder, PoseStack.Pose pose, LineSegment segment, int z) {
        float outerDistance = 100; // TODO consider changing this based on screen size to make sure it always fits
        Vec2 startProjection = fromPolar(outerDistance, segment.startAngle);
        Vec2 endProjection = fromPolar(outerDistance, segment.endAngle);
        Vec2 centerProjection = fromPolar(outerDistance, segment.centerAngle);

        builder.addVertex(pose, segment.startPos.x, segment.startPos.y, z);
        builder.addVertex(pose, centerProjection.x, centerProjection.y, z);
        builder.addVertex(pose, startProjection.x, startProjection.y, z);

        builder.addVertex(pose, segment.endPos.x, segment.endPos.y, z);
        builder.addVertex(pose, centerProjection.x, centerProjection.y, z);
        builder.addVertex(pose, segment.startPos.x, segment.startPos.y, z);

        builder.addVertex(pose, endProjection.x, endProjection.y, z);
        builder.addVertex(pose, centerProjection.x, centerProjection.y, z);
        builder.addVertex(pose, segment.endPos.x, segment.endPos.y, z);
    }
}
