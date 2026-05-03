package phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion;

import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraView {
    // TODO use the list of zones here to decide which zone to prepare render states for
    // TODO optimise for when an angle limitation is already known?

    public final UUID zoneUUID;
    public final Vec2 cameraPos;
    public final ViewOcclusion.Occlusion occlusion;
    public final List<CameraView> childViews;

    @Nullable
    public final SegmentedLine occlusionLine;

    public CameraView(UUID zoneUUID, Vec2 cameraPos, ViewOcclusion.Occlusion occlusion, List<CameraView> childViews, @Nullable SegmentedLine occlusionLine) {
        this.zoneUUID = zoneUUID;
        this.cameraPos = cameraPos;
        this.occlusion = occlusion;
        this.childViews = childViews;
        this.occlusionLine = occlusionLine;
    }

    public static CameraView create(Vec2 cameraPos, Vec2 actualCameraPos, HypoZone zone, Hypoverse hypoverse, int maxChildIterations) {
        return create(cameraPos, actualCameraPos, zone, hypoverse, maxChildIterations, null, null, null);
    }

    public static CameraView create(Vec2 cameraPos, Vec2 actualCameraPos, HypoZone zone, Hypoverse hypoverse, int maxChildIterations, @Nullable Integer entryPortal, @Nullable SegmentedLine occlusionLine, @Nullable SegmentedLine visibilityLine) {
        ViewOcclusion.Occlusion occlusion = ViewOcclusion.calculateOcclusion(zone.portals.values().stream().toList(), cameraPos, actualCameraPos, zone, hypoverse, entryPortal, visibilityLine);

        List<CameraView> childViews = new ArrayList<>();
        if (maxChildIterations > 0) {
            for (SegmentedLine line : occlusion.lines) {
                Portal portal = zone.portals.get(line.lineData.id);
                HypoZone nextZone = hypoverse.getZone(line.lineData.targetZone);
                if (portal != null && nextZone != null) {
                    Portal targetPortal = nextZone.portals.get(portal.getTargetId().portalId);
                    if (targetPortal != null) {
                        Vec2 newCameraPos = Portal.transformZoneVector(cameraPos, portal, targetPortal);
                        Vec2 newActualCameraPos = Portal.transformZoneVector(actualCameraPos, portal, targetPortal);
                        SegmentedLine transformedLine = transformLine(line, portal, targetPortal, cameraPos, newCameraPos);
                        if (transformedLine != null) {
                            CameraView view = create(newCameraPos, newActualCameraPos, nextZone, hypoverse, maxChildIterations - 1, targetPortal.getId(), line, transformedLine);
                            childViews.add(view);
                        }
                    }
                }
            }
        }

        return new CameraView(zone.getUuid(), cameraPos, occlusion, childViews, occlusionLine);
    }

    @Nullable
    public static SegmentedLine transformLine(SegmentedLine line, Portal from, Portal to, Vec2 oldCamPos, Vec2 newCamPos) {
        if (line.segments.isEmpty()) {
            return null;
        }

        List<LineSegment> newSegments = new ArrayList<>();
        for (LineSegment segment : line.segments) {
            Vec2 start = Portal.transformZoneVector(segment.startPos.add(oldCamPos), from, to).add(newCamPos.negated());
            Vec2 end = Portal.transformZoneVector(segment.endPos.add(oldCamPos), from, to).add(newCamPos.negated());
            double startAngle = ViewOcclusion.getAngleFromOrigin(start);
            double endAngle = ViewOcclusion.getAngleFromOrigin(end);
            LineSegment newSegment = new LineSegment(start, end, startAngle, endAngle);
            newSegments.add(newSegment);
        }

        LineSegment first = newSegments.getFirst();
        LineSegment last = newSegments.getLast();

        return new SegmentedLine(first.startPos, last.endPos, first.startAngle, last.endAngle, line.lineData, newSegments);
    }
}
