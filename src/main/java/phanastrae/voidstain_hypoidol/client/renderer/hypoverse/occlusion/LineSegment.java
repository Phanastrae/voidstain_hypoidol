package phanastrae.voidstain_hypoidol.client.renderer.hypoverse.occlusion;

import net.minecraft.world.phys.Vec2;

public class LineSegment {
    public final Vec2 startPos;
    public final Vec2 endPos;
    public final double startAngle;
    public final double endAngle;
    public final double angleRange;
    public final double centerAngle;

    public LineSegment(Vec2 startPos, Vec2 endPos, double startAngle, double endAngle) {
        // start angle to end angle should run counter-clockwise
        this.startPos = startPos;
        this.endPos = endPos;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.angleRange = ViewOcclusion.getRelativeAngle(this.endAngle, this.startAngle);
        this.centerAngle = ViewOcclusion.limitAngleRange(this.startAngle + this.angleRange / 2);
    }

    public boolean angleRangeContains(double testAngle) {
        double relativeTestAngle = ViewOcclusion.getRelativeAngle(testAngle, this.startAngle);
        return 0 <= relativeTestAngle && relativeTestAngle <= this.angleRange;
    }
}
