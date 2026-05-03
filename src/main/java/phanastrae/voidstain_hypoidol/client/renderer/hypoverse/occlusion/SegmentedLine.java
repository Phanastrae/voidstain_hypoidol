package phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SegmentedLine {
    // TODO move lineData into a separate class storing a SegmentedLine and LineData?
    public final ViewOcclusion.LineData lineData;

    public final Vec2 tangent;
    public final Vec2 normal;
    public final Vec2 closestPos;
    public final float lineDistance;

    public final double startAngle;
    public final double endAngle;
    private final double angleRange;
    public final float maxDistance;

    // segments are ordered counter-clockwise, in terms of increasing angle
    public final List<LineSegment> segments;

    public static SegmentedLine create(Vec2 pos1, Vec2 pos2, ViewOcclusion.LineData lineData) {
        double angle1 = ViewOcclusion.getAngleFromOrigin(pos1);
        double angle2 = ViewOcclusion.getAngleFromOrigin(pos2);
        double angleDifference = ViewOcclusion.getRelativeAngle(angle2, angle1);
        // swap start/end if needed
        if (angleDifference <= Math.PI ^ lineData.inverted) {
            return new SegmentedLine(pos1, pos2, angle1, angle2, lineData);
        } else {
            return new SegmentedLine(pos2, pos1, angle2, angle1, lineData);
        }
    }

    private SegmentedLine(Vec2 startPos, Vec2 endPos, double startAngle, double endAngle, ViewOcclusion.LineData lineData) {
        this(startPos, endPos, startAngle, endAngle, lineData, List.of(new LineSegment(startPos, endPos, startAngle, endAngle)));
    }

    public SegmentedLine(Vec2 startPos, Vec2 endPos, double startAngle, double endAngle, ViewOcclusion.LineData lineData, List<LineSegment> segments) {
        this.lineData = lineData;

        this.tangent = endPos.add(startPos.negated()).normalized();
        this.normal = new Vec2(this.tangent.y, -this.tangent.x);
        this.closestPos = startPos.add(tangent.scale(-startPos.dot(tangent)));
        this.lineDistance = this.closestPos.length();

        // TODO update these 4 when occluded?
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.angleRange = ViewOcclusion.getRelativeAngle(endAngle, startAngle);
        this.maxDistance = Math.max(startPos.length(), endPos.length());

        this.segments = new ArrayList<>(segments);
    }

    public boolean angleRangeContains(double testAngle) {
        double relativeTestAngle = ViewOcclusion.getRelativeAngle(testAngle, this.startAngle);
        return 0 <= relativeTestAngle && relativeTestAngle <= this.angleRange;
    }

    public void addSegment(LineSegment segment) {
        if (0.001 <= segment.angleRange && segment.angleRange < Math.PI) {
            this.segments.add(segment);
        }
    }

    public void splitAtAngle(double angle) {
        Vec2 intersect = getRayIntersect(angle);
        if (intersect != null) {
            List<LineSegment> oldSegments = new ArrayList<>(this.segments);
            this.segments.clear();

            for (LineSegment segment : oldSegments) {
                if (segment.angleRangeContains(angle)) {
                    this.addSegment(new LineSegment(segment.startPos, intersect, segment.startAngle, angle));
                    this.addSegment(new LineSegment(intersect, segment.endPos, angle, segment.endAngle));
                } else {
                    this.addSegment(segment);
                }
            }
        }
    }

    public void cullAnyPartsBehind(SegmentedLine keepLine) {
        List<LineSegment> oldSegments = new ArrayList<>(this.segments);
        this.segments.clear();

        for (LineSegment segment : oldSegments) {
            Vec2 relStart = segment.startPos.add(keepLine.closestPos.negated());
            Vec2 relEnd = segment.endPos.add(keepLine.closestPos.negated());
            boolean startStays = relStart.dot(keepLine.closestPos) > 0;
            boolean endStays = relEnd.dot(keepLine.closestPos) > 0;
            if (startStays && endStays) {
                this.segments.add(segment);
            } else {
                Vec2 intersect = ViewOcclusion.getIntersect(keepLine, this);
                double intersectAngle = ViewOcclusion.getAngleFromOrigin(intersect);
                if (startStays) {
                    this.segments.add(new LineSegment(segment.startPos, intersect, segment.startAngle, intersectAngle));
                } else if (endStays) {
                    this.segments.add(new LineSegment(intersect, segment.endPos, intersectAngle, segment.endAngle));
                }
                // else cull completely
            }
        }
    }

    public void occludeRange(double startAngle, double endAngle) {
        List<LineSegment> oldSegments = new ArrayList<>(this.segments);
        this.segments.clear();

        double range = ViewOcclusion.getRelativeAngle(endAngle, startAngle);
        for (LineSegment segment : oldSegments) {
            double relStart = ViewOcclusion.getRelativeAngle(segment.startAngle, startAngle);
            double relEnd = ViewOcclusion.getRelativeAngle(segment.endAngle, startAngle);

            boolean startInRange = 0 <= relStart && relStart <= range;
            boolean endInRange = 0 <= relEnd && relEnd <= range;
            if (startInRange && endInRange) {
                // occlude completely
            } else if (startInRange) {
                // occlude from start to intersect
                Vec2 intersect = getRayIntersect(endAngle);
                if (intersect != null) {
                    this.addSegment(new LineSegment(intersect, segment.endPos, endAngle, segment.endAngle));
                }
            } else if (endInRange) {
                // occlude from intersect to end
                Vec2 intersect = getRayIntersect(startAngle);
                if (intersect != null) {
                    this.addSegment(new LineSegment(segment.startPos, intersect, segment.startAngle, startAngle));
                }
            } else {
                // segment start/end is outside of start/end occlusion range
                if (segment.angleRangeContains(startAngle)) {
                    // occlude middle of segment
                    Vec2 startIntersect = getRayIntersect(startAngle);
                    if (startIntersect != null) {
                        this.addSegment(new LineSegment(segment.startPos, startIntersect, segment.startAngle, startAngle));
                    }

                    Vec2 endIntersect = getRayIntersect(endAngle);
                    if (endIntersect != null) {
                        this.addSegment(new LineSegment(endIntersect, segment.endPos, endAngle, segment.endAngle));
                    }
                } else {
                    // occlude nothing
                    this.addSegment(segment);
                }
            }
        }
    }

    @Nullable
    public Vec2 getRayIntersect(double rayAngle) {
        // TODO prevent rays from going backwards?
        Vec2 rayTangent = new Vec2(Mth.cos(rayAngle), Mth.sin(rayAngle));
        Vec2 rayNormal = new Vec2(rayTangent.y, -rayTangent.x);
        float denom = rayNormal.dot(this.tangent);
        if (Math.abs(denom) < 1E-5) {
            // segment is parallel to ray
            return null;
        } else {
            float u = -rayNormal.dot(this.closestPos) / denom;
            return this.closestPos.add(this.tangent.scale(u));
        }
    }
}
