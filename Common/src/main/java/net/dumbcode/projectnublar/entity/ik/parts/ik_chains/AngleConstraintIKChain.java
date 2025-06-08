package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.ChainCommonClass;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Debug;

public abstract class AngleConstraintIKChain extends StretchingIKChain {
    private double floorAngle;
    private double floorAngleSize = 90;
    private Vec3 base = Vec3.ZERO;

    public AngleConstraintIKChain(double floorAngle, double... lengths) {
        super(lengths);
        this.floorAngle = floorAngle;
    }

    public AngleConstraintIKChain(double floorAngle, Segment... segments) {
        super(segments);
        this.floorAngle = floorAngle;
    }

    public double getFloorAngle() {
        return floorAngle;
    }

    public double getFloorAngleSize() {
        return floorAngleSize;
    }

    public Vec3 getBase() {
        return base;
    }

    @Override
    public void iterate(Vec3 target, Vec3 base) {
        this.base = base;
        super.iterate(target, base);
    }

    @Override
    public void reachForwards(Vec3 target) {
        this.endJoint = target;

        Segment rootNextSegment = this.getLast();

        //Calculate the direction form the endjoint to the last segment and then extend it to the length of the segment to get our new position
        Vec3 bastTargetDir = rootNextSegment.getPosition().subtract(this.endJoint).normalize();
        Vec3 rootNewPos = this.endJoint.add(bastTargetDir.scale(rootNextSegment.length));

        // Direction from the endjoint to the base, hip, of the leg.
        Vec3 footToBase = this.base.subtract(this.endJoint).normalize().add(this.endJoint);

        Vec3 rootReferencePoint = this.rotatePointOnLegPlane(footToBase, this.endJoint, (90 + this.getFloorAngle()));

        Vec3 rootDotBaseDir = rootReferencePoint.subtract(this.endJoint).normalize();
        Vec3 rootDotTargetDir = rootNewPos.subtract(this.endJoint).normalize();

        double angleRoot = Math.toDegrees(Math.acos(rootDotBaseDir.dot(rootDotTargetDir)));

        if (angleRoot > this.getFloorAngleSize()) {
            double angleDifference = angleRoot - this.getFloorAngleSize();

            Vec3 rotationAxis = MathUtil.getUpDirection(this.endJoint, rootNewPos, rootReferencePoint).normalize().dot(this.getLegPlane().normalize()) > 0 ? this.getLegPlane().normalize() : this.getLegPlane().normalize().reverse();;

            rootNextSegment.move(MathUtil.rotatePointOnAPlaneAround(rootNewPos, this.endJoint, angleDifference, rotationAxis));
        } else {
            rootNextSegment.move(rootNewPos);
        }

        for (int i = this.segments.size() - 1; i > 0; i--) {
            Segment currentSegment = this.get(i);
            Vec3 currentVec3 = currentSegment.getPosition();
            Segment nextSegment = this.get(i - 1);

            Vec3 targetDir = nextSegment.getPosition().subtract(currentVec3).normalize();
            Vec3 newPos = currentVec3.add(targetDir.scale(nextSegment.length));

            Vec3 referencePoint = this.rotatePointOnLegPlane(this.getJoints().get(i + 1), currentVec3, -nextSegment.angleOffset);

            if (i == 1) {
                referencePoint = this.rotatePointOnLegPlane(currentVec3.add(this.getDownNormalOnLegPlane().reverse()), currentVec3, -nextSegment.angleOffset);
            }

            Vec3 dotBaseDir = referencePoint.subtract(currentVec3).normalize();
            Vec3 dotTargetDir = newPos.subtract(currentVec3).normalize();

            double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

            if (angle > nextSegment.angleSize) {
                double angleDifference = angle - nextSegment.angleSize;

                Vec3 rotationAxis = MathUtil.getUpDirection(currentVec3, newPos, referencePoint).normalize().dot(this.getLegPlane().normalize()) > 0 ? this.getLegPlane().normalize() : this.getLegPlane().normalize().reverse();;

                nextSegment.move(MathUtil.rotatePointOnAPlaneAround(newPos, currentVec3, -angleDifference, rotationAxis));
            } else {
                nextSegment.move(newPos);
            }
        }
    }

    @Override
    public void reachBackwards(Vec3 base) {
        this.getFirst().move(base);

        Vec3 rootTargetDir = this.get(1).getPosition().subtract(base).normalize();
        Vec3 rootNewPos = base.add(rootTargetDir.scale(this.getFirst().length));

        Vec3 rootReferencePoint = this.rotatePointOnLegPlane(base.add(this.getDownNormalOnLegPlane()), base, this.getFirst().angleOffset);

        Vec3 rootDotBaseDir = rootReferencePoint.subtract(base).normalize();
        Vec3 rootDotTargetDir = rootNewPos.subtract(base).normalize();

        double rootAngle = Math.toDegrees(Math.acos(rootDotBaseDir.dot(rootDotTargetDir)));

        if (rootAngle > this.getFirst().angleSize) {
            double angleDifference = rootAngle - this.getFirst().angleSize;

            Vec3 rotationAxis = MathUtil.getUpDirection(base, rootNewPos, rootReferencePoint).normalize().dot(this.getLegPlane().normalize()) > 0 ? this.getLegPlane().normalize() : this.getLegPlane().normalize().reverse();;

            this.get(1).move(MathUtil.rotatePointOnAPlaneAround(rootNewPos, base, angleDifference, rotationAxis));
        } else {
            this.get(1).move(rootNewPos);
        }


        for (int i = 1; i < this.segments.size() - 1; i++) {
            Segment currentSegment = this.get(i);
            Vec3 currentVec3 = currentSegment.getPosition();
            Segment nextSegment = this.get(i + 1);

            Vec3 targetDir = nextSegment.getPosition().subtract(currentVec3).normalize();
            Vec3 newPos = currentVec3.add(targetDir.scale(currentSegment.length));

            Vec3 referencePoint = this.rotatePointOnLegPlane(this.get(i - 1).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);

            Vec3 dotBaseDir = referencePoint.subtract(currentVec3).normalize();
            Vec3 dotTargetDir = newPos.subtract(currentVec3).normalize();

            double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

            if (angle > currentSegment.angleSize) {
                double angleDifference = angle - currentSegment.angleSize;

                Vec3 rotationAxis = MathUtil.getUpDirection(currentVec3, newPos, referencePoint).normalize().dot(this.getLegPlane().normalize()) > 0 ? this.getLegPlane().normalize() : this.getLegPlane().normalize().reverse(); // ;

                nextSegment.move(MathUtil.rotatePointOnAPlaneAround(newPos, currentVec3, angleDifference, rotationAxis));
            } else {
                nextSegment.move(newPos);
            }
        }

        Segment currentSegment = this.getLast();
        Vec3 currentVec3 = currentSegment.getPosition();

        Vec3 targetDir = this.endJoint.subtract(currentVec3).normalize();
        Vec3 newPos = currentVec3.add(targetDir.scale(currentSegment.length));

        Vec3 referencePoint = this.rotatePointOnLegPlane(this.get(this.segments.size() - 2).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);

        Vec3 dotBaseDir = referencePoint.subtract(currentVec3).normalize();
        Vec3 dotTargetDir = newPos.subtract(currentVec3).normalize();

        double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

        if (angle > currentSegment.angleSize) {
            double angleDifference = angle - currentSegment.angleSize;

            Vec3 rotationAxis = MathUtil.getUpDirection(currentVec3, newPos, referencePoint).normalize().dot(this.getLegPlane().normalize()) > 0 ? this.getLegPlane().normalize() : this.getLegPlane().normalize().reverse(); // ;

            this.endJoint = MathUtil.rotatePointOnAPlaneAround(newPos, currentVec3, angleDifference, rotationAxis);
        } else {
            this.endJoint = newPos;
        }
    }

    public abstract Vec3 getDownNormalOnLegPlane();

    public Vec3 rotatePointOnLegPlane(Vec3 point, Vec3 base, double angle) {
        return MathUtil.rotatePointOnAPlaneAround(point, base, angle, this.getLegPlane());
    }

    public Vec3 getLegPlane() {
        return MathUtil.getNormalClosestTo(this.getFirst().getPosition(), this.endJoint, this.getStretchingPos(this.endJoint, this.getFirst().getPosition()), this.getReferencePoint());
    }

    public abstract Vec3 getReferencePoint();
}
