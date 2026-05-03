package phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import phanastrae.voidstain_hypoidol.common.hypoverse.HypoZone;
import phanastrae.voidstain_hypoidol.common.hypoverse.Hypoverse;
import phanastrae.voidstain_hypoidol.common.hypoverse.Portal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ViewOcclusion {
    // TODO optimise this entire proess

    public static Occlusion calculateOcclusion(List<Portal> portals, Vec2 cameraPos, Vec2 actualCameraPos, HypoZone zone, Hypoverse hypoverse, @Nullable Integer entryPortal, @Nullable SegmentedLine visibilityLine) {
        // 1) get a list of lines from portals
        List<SegmentedLine> lines = new ArrayList<>();
        for (Portal portal : portals) {
            HypoZone targetZone = portal.getTargetZone(zone, hypoverse);
            Portal targetPortal = targetZone == null ? null : targetZone.portals.get(portal.getTargetId().portalId);
            if (targetPortal != null) {
                Vec2 relativeStart = portal.getStartPos().add(cameraPos.negated());
                Vec2 relativeEnd = portal.getEndPos().add(cameraPos.negated());

                boolean inverted = false;
                // check if player is between the portal edges
                if (relativeStart.dot(relativeEnd) < 0) {
                    Vec2 relativeCenter = portal.getCenter().add(cameraPos.negated());
                    Vec2 actualRelativeCenter = portal.getCenter().add(actualCameraPos.negated());
                    int playerSidedness = -Portal.getPositionSidedness(portal.getNormal().dot(relativeCenter));
                    int playerActualSidedness = -Portal.getPositionSidedness(portal.getNormal().dot(actualRelativeCenter));
                    // flip center projection if needed
                    if (playerActualSidedness != playerSidedness) {
                        inverted = true;
                    }
                }

                if (entryPortal != null) {
                    if (entryPortal == portal.getId()) {
                        // if entryPortal exists, is should be inverted (unless normal inversion conditions apply, in which it should *not* be inverted)
                        inverted = !inverted;
                    } else {
                        // if entry portal exists, only it may be the inverted portal
                        inverted = false;
                    }
                }

                LineData lineData = new LineData(
                        inverted,
                        portal.getId(),
                        portal.getCenter(),
                        portal.getAngle(),
                        targetPortal.getCenter(),
                        targetPortal.getAngle(),
                        targetZone.getUuid()
                );
                SegmentedLine segment = SegmentedLine.create(relativeStart, relativeEnd, lineData);
                lines.add(segment);
            }
        }

        // 2) initially sort by distance from player to closest point on the entire line
        // TODO does this have much of an effect? is this sorting in the right direction?
        lines.sort(Comparator.comparing(s -> s.lineDistance * (s.lineData.inverted ? -1 : 1)));

        // 3) insert one by one into the list, occluding/splitting/being-occluded-by the other existing lines in the list
        Occlusion occlusion = new Occlusion();
        for (SegmentedLine segment : lines) {
            occlusion.addLine(segment);
        }

        // 4) if visibilityLine exists, make sure everything is inside its range
        if (visibilityLine != null) {
            occlusion.cullPiecesOutOfRange(visibilityLine);
        }

        return occlusion;
    }

    public static double getAngleFromOrigin(Vec2 pos) {
        return limitAngleRange(Math.atan2(pos.y, pos.x));
    }

    public static double limitAngleRange(double angle) {
        return Mth.positiveModulo(angle, Math.TAU);
    }

    public static double getRelativeAngle(double angle, double relativeTo) {
        return limitAngleRange(angle - relativeTo);
    }

    public static void occludeSegmentedLines(SegmentedLine line1, SegmentedLine line2) {
        // TODO check the 0.001 and the 0.999 they might not be good enough ranges

        // if one line is inverted, then we can just skip this all and put that one in front
        if (line1.lineData.inverted && !line2.lineData.inverted) {
            cullAnyPartsBehind(line1, line2);
            return;
        } else if (line2.lineData.inverted && !line1.lineData.inverted) {
            cullAnyPartsBehind(line2, line1);
            return;
        }
        // TODO what if both are inverted?
        // if this doesn't happen, then we assume that all lines are correctly oriented from start to end

        // if lines don't overlap, then lines do not need occlusion
        if (!line1.angleRangeContains(line2.startAngle) && !line1.angleRangeContains(line2.endAngle) && !line2.angleRangeContains(line1.startAngle) && !line2.angleRangeContains(line1.endAngle)) {
            return;
        }

        // if one lines's maxDistance is less than the other's lineDistance, then we know it is in front and so can just do the occlusion now
        // TODO check again if this makes sense, and re-enable if it does
        if (line1.maxDistance < line2.lineDistance) {
//            occlude(line1, line2);
//            return;
        } else if (line2.maxDistance < line1.lineDistance) {
//            occlude(line2, line1);
//            return;
        }

        // check tangents
        if (linesParallel(line1, line2)) {
            // if parallel, just put the closer line in front, and occlude the one behind
            if (line1.lineDistance <= line2.lineDistance) {
                occlude(line1, line2);
            } else {
                occlude(line2, line1);
            }
        } else {
            // if not parallel, find the intersect of the two lines
            Vec2 intersect = getIntersect(line1, line2);

            // assume camera is at origin
            double intersectAngle = getAngleFromOrigin(intersect);
            Vec2 intersectDirection = intersect.normalized(); // TODO might this cause problems (function approximates to zero) if very close to intersect?

            boolean tangent1FacesAwayFromCameraMore = intersectDirection.dot(line1.tangent) > intersectDirection.dot(line2.tangent);

            // if both lines cross the intersect, then split them both and put one part of each in front, occluding the other
            if (line1.angleRangeContains(intersectAngle) && line2.angleRangeContains(intersectAngle)) {
                line1.splitAtAngle(intersectAngle);
                line2.splitAtAngle(intersectAngle);

                if (tangent1FacesAwayFromCameraMore) {
                    // tangent 1 faces away from camera more than tangent 2
                    // so start2 is behind start1, end1 is behind end2
                    occludeInRange(line1, line2, line1.startAngle, intersectAngle);
                    occludeInRange(line2, line1, intersectAngle, line2.endAngle);
                } else {
                    // tangent 2 faces away from camera more than tangent 1
                    // so start1 is behind start2, end2 is behind end1
                    occludeInRange(line2, line1, line2.startAngle, intersectAngle);
                    occludeInRange(line1, line2, intersectAngle, line1.endAngle);
                }
            } else {
                // if one or neither lines crosses the intersect, then choose the side that they both lie on (which must exist, since their angle ranges overlap at some point)
                // for that side, check which one is in front, and place it in front, occluding the one behind
                // TODO what if rel angle is very very slightly less than zero (and thus roughly TAU) ? might want to use signed angle instead to be safe?
                // TODO should signed angle be used elsewhere?
                if (getRelativeAngle(line1.endAngle, intersectAngle) < Math.PI && getRelativeAngle(line2.endAngle, intersectAngle) < Math.PI) {
                    // both lines are on the counter-clockwise/left side of the intersect
                    if (tangent1FacesAwayFromCameraMore) {
                        // tangents goes from start angle to end angle, so counter-clockwise
                        // so this means that line 1 is behind line 2
                        occlude(line2, line1);
                    } else {
                        // line 2 is behind line 1
                        occlude(line1, line2);
                    }
                } else {
                    // both lines are on the clockwise/right side of the intersect
                    if (tangent1FacesAwayFromCameraMore) {
                        // tangents goes from start angle to end angle, so counter-clockwise
                        // but we are on counterclockwise side, so go the other way around
                        // so this means that line 2 is behind line 1
                        occlude(line1, line2);
                    } else {
                        // line 1 is behind line 2
                        occlude(line2, line1);
                    }
                }
            }
        }
    }

    public static boolean linesParallel(SegmentedLine line1, SegmentedLine line2) {
        return Math.abs(line1.tangent.dot(line2.tangent)) > 0.99999;
    }

    public static Vec2 getIntersect(SegmentedLine line1, SegmentedLine line2) {
        float u = line2.normal.dot(line2.closestPos.add(line1.closestPos.negated())) / line2.normal.dot(line1.tangent);
        return line1.closestPos.add(line1.tangent.scale(u));
    }

    public static void cullAnyPartsBehind(SegmentedLine keepLine, SegmentedLine culledLine) {
        culledLine.cullAnyPartsBehind(keepLine);
    }

    public static void occlude(SegmentedLine front, SegmentedLine behind) {
        // TODO this can probably be optimised, as can a lot of things here
        for (LineSegment segment : front.segments) {
            behind.occludeRange(segment.startAngle, segment.endAngle);
        }
    }

    public static void occludeInRange(SegmentedLine front, SegmentedLine behind, double startAngle, double endAngle) {
        double angleRange = getRelativeAngle(endAngle, startAngle);
        for (LineSegment segment : front.segments) {
            double relStart = getRelativeAngle(segment.startAngle, startAngle);
            double relEnd = getRelativeAngle(segment.endAngle, startAngle);
            // limit to range (-PI, PI]
            if (relStart > Math.PI) {
                relStart -= Math.TAU;
            }
            if (relEnd > Math.PI) {
                relEnd -= Math.TAU;
            }
            relStart = Math.clamp(relStart, 0, angleRange);
            relEnd = Math.clamp(relEnd, 0, angleRange);
            if (relStart < relEnd) {
                behind.occludeRange(limitAngleRange(startAngle + relStart), limitAngleRange(startAngle + relEnd));
            }
        }
    }

    public static class Occlusion {
        public final List<SegmentedLine> lines = new ArrayList<>();

        public void addLine(SegmentedLine newLine) {
            for (SegmentedLine line : this.lines) {
                occludeSegmentedLines(line, newLine);
            }
            this.lines.add(newLine);
            this.removeEmptyLines();
        }

        public void cullPiecesOutOfRange(SegmentedLine visibilityLine) {
            // TODO this can probably be optimised quite a lot
            List<Double> splitAngles = new ArrayList<>();
            // collect all start/end angles in a list
            for (LineSegment segment : visibilityLine.segments) {
                splitAngles.add(segment.startAngle);
                splitAngles.add(segment.endAngle);
            }
            // deduplicate (this probably shouldn't happen) // TODO is this needed?
            splitAngles = splitAngles.stream().distinct().toList();

            for (SegmentedLine line : this.lines) {
                // split line into segments
                for (double angle : splitAngles) {
                    // TODO once again: this thing can probably be optimised a lot
                    line.splitAtAngle(angle);
                }

                List<LineSegment> segments = new ArrayList<>(line.segments);
                line.segments.clear();

                // remove any hidden segments
                for (LineSegment segment : segments) {
                    boolean visible = false;
                    for (LineSegment visbilitySegment : visibilityLine.segments) {
                        if (visbilitySegment.angleRangeContains(segment.centerAngle)) {
                            visible = true;
                            break;
                        }
                    }
                    if (visible) {
                        line.segments.add(segment);
                    }
                }
            }

            this.removeEmptyLines();
        }

        public void removeEmptyLines() {
            this.lines.removeIf(line -> line.segments.isEmpty());
        }
    }

    public static class LineData {
        public final boolean inverted;
        public final int id;
        public final Vec2 center;
        public final float angle;
        public final Vec2 targetCenter;
        public final float targetAngle;
        public final UUID targetZone;

        public LineData(boolean inverted, int id, Vec2 center, float angle, Vec2 targetCenter, float targetAngle, UUID targetZone) {
            this.inverted = inverted;
            this.id = id;
            this.center = center;
            this.angle = angle;
            this.targetCenter = targetCenter;
            this.targetAngle = targetAngle;
            this.targetZone = targetZone;
        }
    }
}