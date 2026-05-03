package phanastrae.voidstain_hypoidol.common.hypoverse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Portal {
    public static final Codec<Portal> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Vec2.CODEC.fieldOf("center").forGetter(Portal::getCenter),
                    Codec.FLOAT.fieldOf("length").forGetter(Portal::getLength),
                    Codec.FLOAT.fieldOf("angle").forGetter(Portal::getAngle),
                    Codec.INT.fieldOf("id").forGetter(Portal::getId),
                    PortalId.CODEC.fieldOf("target").forGetter(Portal::getTargetId)
            ).apply(i, Portal::new)
    );
    public static final StreamCodec<ByteBuf, Vec2> VEC2_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Vec2 decode(ByteBuf input) {
            return new Vec2(input.readFloat(), input.readFloat());
        }

        @Override
        public void encode(ByteBuf output, Vec2 value) {
            output.writeFloat(value.x);
            output.writeFloat(value.y);
        }
    };


    private final Vec2 center;
    private final float length;
    private final float angle;
    private final int id;
    private final PortalId targetId;

    private final float halfLength;
    private final Vec2 tangent;
    private final Vec2 normal;
    private final Vec2 startPos;
    private final Vec2 endPos;

    public Portal(Vec2 center, float length, float angle, int id, PortalId targetId) {
        this.center = center;
        this.length = length;
        this.angle = angle;
        this.id = id;
        this.targetId = targetId;

        this.halfLength = length / 2;
        float angleRads = angle * Mth.TWO_PI / 360.0f;
        float cos = Mth.cos(angleRads);
        float sin = Mth.sin(angleRads);

        this.tangent = new Vec2(cos, sin); // starts aimed to the right, rotates counter-clockwise
        this.normal = new Vec2(sin, -cos); // starts aimed downwards, rotates counter-clockwise
        this.startPos = this.center.add(this.tangent.scale(-0.5f));
        this.endPos = this.center.add(this.tangent.scale(0.5f));
    }

    public Vec2 getCenter() {
        return this.center;
    }

    public float getLength() {
        return this.length;
    }

    public float getAngle() {
        return this.angle;
    }

    public int getId() {
        return this.id;
    }

    public PortalId getTargetId() {
        return this.targetId;
    }

    @Nullable
    public HypoZone getTargetZone(HypoZone thisZone, Hypoverse hypoverse) {
        if (this.targetId.zoneUUID.equals(thisZone.uuid)) {
            return thisZone;
        } else {
            return hypoverse.getZone(this.targetId.zoneUUID);
        }
    }

    public Vec2 getTangent() {
        return this.tangent;
    }

    public Vec2 getNormal() {
        return this.normal;
    }

    public Vec2 getStartPos() {
        return this.startPos;
    }

    public Vec2 getEndPos() {
        return this.endPos;
    }

    public Vec2 zonePosToRelativePos(Vec2 relativePos) {
        return this.zonePosToRelativePos(relativePos.x, relativePos.y);
    }

    public Vec2 zonePosToRelativePos(float zoneX, float zoneY) {
        return new Vec2(zoneX - this.center.x, zoneY - this.center.y);
    }

    public Vec2 relativePosToZonePos(Vec2 relativePos) {
        return new Vec2(relativePos.x + this.center.x, relativePos.y + this.center.y);
    }

    public static Vec2 transformRelativeVector(Vec2 relativeVector, Portal from, Portal to) {
        return to.tangent.scale(relativeVector.dot(from.tangent)).add(to.normal.scale(relativeVector.dot(from.normal)));
    }

    public static Vec2 transformZoneVector(Vec2 zoneVector, Portal from, Portal to) {
        Vec2 relativeVector = from.zonePosToRelativePos(zoneVector);
        Vec2 newRelativeVector = Portal.transformRelativeVector(relativeVector, from, to);
        return to.relativePosToZonePos(newRelativeVector);
    }

    // returns intersect distance, or Float.POSITIVE_INFINITY if it misses
    public float zoneRayIntersects(Vec2 start, Vec2 end) {
        return this.relativeRayIntersects(this.zonePosToRelativePos(start), this.zonePosToRelativePos(end));
    }

    // returns intersect distance, or Float.POSITIVE_INFINITY if it misses
    public float relativeRayIntersects(Vec2 start, Vec2 end) {
        float startDotNormal = start.dot(this.normal);
        if (getPositionSidedness(startDotNormal) == getPositionSidedness(end)) {
            // ray does not cross portal line
            return Float.POSITIVE_INFINITY;
        }
        // ray does cross over portal line
        Vec2 dif = end.add(start.negated());
        float difDotNormal = dif.dot(this.normal);

        Vec2 intersect;
        if (difDotNormal < 1E-6) {
            // ray is too short, avoid division by zero errors by just approximating the intersect
            intersect = start.add(start.scale(-startDotNormal));
        } else {
            intersect = start.add(dif.scale(-startDotNormal / difDotNormal));
        }
        float intersectPosition = this.tangent.dot(intersect);
        if (-this.halfLength <= intersectPosition && intersectPosition <= this.halfLength) {
            return intersect.add(start.negated()).length();
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    public int getPositionSidedness(Vec2 pos) {
        return getPositionSidedness(pos.dot(this.normal));
    }

    public static int getPositionSidedness(float dot) {
        // intentionally return 1 for dot = 0, so that the entire plane is divided into only 2 pieces
        return dot >= 0 ? 1 : -1;
    }

    public static class PortalId {
        public static final Codec<PortalId> CODEC = RecordCodecBuilder.create(i -> i.group(
                        UUIDUtil.CODEC.fieldOf("zone_uuid").forGetter(pid -> pid.zoneUUID),
                        Codec.INT.fieldOf("id").forGetter(pid -> pid.portalId)
                ).apply(i, PortalId::new)
        );
        public static final StreamCodec<FriendlyByteBuf, PortalId> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                pid -> pid.zoneUUID,
                ByteBufCodecs.INT,
                pid -> pid.portalId,
                PortalId::new
        );

        public final UUID zoneUUID;
        public final int portalId;

        public PortalId(UUID zoneUUID, int portalId) {
            this.zoneUUID = zoneUUID;
            this.portalId = portalId;
        }
    }
}
