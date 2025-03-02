package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.dumbcode.projectnublar.entity.ik.util.PrAnCommonClass;
import net.minecraft.world.phys.Vec3;

public abstract class AngleConstraintIKChain extends StretchingIKChain {

    public AngleConstraintIKChain(double... lengths) {
        super(lengths);
    }

    public AngleConstraintIKChain(Segment... segments) {
        super(segments);
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

            Vec3 rotationAxis = MathUtil.getUpDirection(base, rootNewPos, rootReferencePoint);

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

                Vec3 rotationAxis = MathUtil.getUpDirection(currentVec3, newPos, rootReferencePoint);

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

            Vec3 rotationAxis = MathUtil.getUpDirection(currentVec3, newPos, rootReferencePoint);

            this.endJoint = MathUtil.rotatePointOnAPlaneAround(newPos, currentVec3, angleDifference, rotationAxis);
        } else {
            this.endJoint = newPos;
        }
    }

    /*
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

            Vec3 rotationAxis = MathUtil.getUpDirection(base, rootNewPos, rootReferencePoint);

            rootNewPos = MathUtil.rotatePointOnAPlaneAround(rootNewPos, base, angleDifference, rotationAxis);
        }

        this.get(1).move(rootNewPos);

        for (int i = 1; i < this.segments.size() - 1; i++) {
            Segment currentSegment = this.get(i);
            Segment nextSegment = this.get(i + 1);

            Vec3 targetDir = nextSegment.getPosition().subtract(currentSegment.getPosition()).normalize();
            Vec3 newPos = currentSegment.getPosition().add(targetDir.scale(currentSegment.length));

            Vec3 referencePoint = this.rotatePointOnLegPlane(this.get(i - 1).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);

            Vec3 dotBaseDir = referencePoint.subtract(currentSegment.getPosition()).normalize();
            Vec3 dotTargetDir = newPos.subtract(currentSegment.getPosition()).normalize();

            double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

            if (angle > currentSegment.angleSize) {
                double angleDifference = angle - currentSegment.angleSize;

                Vec3 rotationAxis = MathUtil.getUpDirection(currentSegment.getPosition(), newPos, referencePoint);

                newPos = MathUtil.rotatePointOnAPlaneAround(newPos, currentSegment.getPosition(), -angleDifference, rotationAxis);
            }

            nextSegment.move(newPos);
        }

        Segment currentSegment = this.getLast();
        Vec3 nextSegment = this.endJoint;

        Vec3 targetDir = nextSegment.subtract(currentSegment.getPosition()).normalize();
        Vec3 newPos = currentSegment.getPosition().add(targetDir.scale(currentSegment.length));

        Vec3 referencePoint = this.rotatePointOnLegPlane(this.get(this.segments.size() - 2).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);

        Vec3 dotBaseDir = referencePoint.subtract(currentSegment.getPosition()).normalize();
        Vec3 dotTargetDir = newPos.subtract(currentSegment.getPosition()).normalize();

        double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

        if (angle > currentSegment.angleSize) {
            double angleDifference = angle - currentSegment.angleSize;

            Vec3 rotationAxis = MathUtil.getUpDirection(currentSegment.getPosition(), newPos, referencePoint);

            newPos = MathUtil.rotatePointOnAPlaneAround(newPos, currentSegment.getPosition(), -angleDifference, rotationAxis);
        }

        this.endJoint = newPos;
    }
    */

    public abstract Vec3 getDownNormalOnLegPlane();

    public Vec3 rotatePointOnLegPlane(Vec3 point, Vec3 base, double angle) {
        return MathUtil.rotatePointOnAPlaneAround(point, base, angle, this.getLegPlane());
    }

    /*
    @Override
    public void reachForwards(Vec3 target) {
        this.endJoint = target;

        Vec3 endCurrentSegment = this.endJoint;
        Segment endNextSegment = this.getLast();

        Vec3 endTargetDir = endNextSegment.getPosition().subtract(endCurrentSegment).normalize();
        Vec3 endNewPos = endCurrentSegment.add(endTargetDir.scale(endNextSegment.length));

        Vec3 referencePoint = this.rotatePointOnLegPlane(this.get(this.segments.size() - 2).getPosition(), endCurrentSegment, endNextSegment.angleOffset);

        Vec3 dotBaseDir = referencePoint.subtract(endCurrentSegment).normalize();
        Vec3 dotTargetDir = endNewPos.subtract(endCurrentSegment).normalize();

        double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

        if (angle > endNextSegment.angleSize) {
            double angleDifference = angle - endNextSegment.angleSize;

            Vec3 rotationAxis = MathUtil.getUpDirection(endCurrentSegment, endNewPos, referencePoint);

            endNewPos = MathUtil.rotatePointOnAPlaneAround(endNewPos, endCurrentSegment, angleDifference, endNextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());
        }

        this.endJoint = endNewPos;

        this.getLast().move(this.moveSegment(this.getLast().getPosition(), this.endJoint, this.getLast().length));
        for (int i = this.segments.size() - 1; i > 0; i--) {
            Segment currentSegment = this.segments.get(i);
            Segment nextSegment = this.segments.get(i - 1);

            nextSegment.move(this.moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), nextSegment.length));
        }
    }

     */

    public Vec3 getLegPlane() {
        return MathUtil.getNormalClosestTo(this.getFirst().getPosition(), this.endJoint, this.getStretchingPos(this.endJoint, this.getFirst().getPosition()), this.getReferencePoint());
    }

    public abstract Vec3 getReferencePoint();

    public Vec3 getConstrainedPosForRootSegment() {
        Vec3 C = new Vec3(0, 1, 0);
        return this.getConstrainedPosForRootSegment(C);
    }

    /**
     * Get the angle at the given index in degrees
     * @param index the index of the segment you want to get the angle of
     * @return the angle at the given index in degrees
     */
    public double getAngleAt(int index) {
        if (index < 1) {
            PrAnCommonClass.throwInDevOnly(new IllegalArgumentException("Called **getAngleAt** with an index of 0. The index always needs to be at least 1" +
                    "Min Example: this.getAngleAt(1)"));
            return 0;
        }
        
        if (index > this.segments.size() - 2) {
            PrAnCommonClass.throwInDevOnly(new IllegalArgumentException("Called **getAngleAt** with an index bigger then the segment about -1. The index always needs to be at least 1 less then the total segments." +
                    "Max Example: this.getAngleAt(this.segments.size() - 2)"));
            return 0;
        }
        
        Segment previousSegment = this.segments.get(index - 1);
        Segment currentSegment = this.segments.get(index);
        Segment nextSegment = this.segments.get(index + 1);

        Vec3 baseDir = previousSegment.getPosition().subtract(currentSegment.getPosition()).normalize();
        Vec3 targetDir = nextSegment.getPosition().subtract(currentSegment.getPosition()).normalize();

        return Math.toDegrees(Math.acos(baseDir.dot(targetDir)));
    }

    public Vec3 getConstrainedPosForRootSegment(Vec3 downVector) {
        double angle = Math.toDegrees(MathUtil.calculateAngle(this.getFirst().getPosition(), this.segments.get(1).getPosition(), this.getFirst().getPosition().add(downVector)));
        double clampedAngle = Math.min(this.getFirst().angleSize, angle);

        if (clampedAngle == angle) return this.segments.get(1).getPosition();

        double angleDelta = clampedAngle - angle;

        //Vec3 normal = MathUtil.getNormalClosestTo(this.segments.get(1).getPosition(), this.getFirst().getPosition(), this.segments.get(2).getPosition(), this.getReferencePoint());
        return MathUtil.rotatePointOnAPlaneAround(this.segments.get(1).getPosition(), this.getFirst().getPosition(), angleDelta, this.getLegPlane());
    }

    public Vec3 getConstrainedPositions(Vec3 reference, Segment middle, Vec3 endpoint) {
        //Vec3 normal = MathUtil.getNormalClosestTo(endpoint, middle.getPosition(), reference, this.getReferencePoint());

        Vec3 referencePoint = MathUtil.rotatePointOnAPlaneAround(reference, middle.getPosition(), middle.angleOffset, this.getLegPlane());

        double angle = Math.toDegrees(MathUtil.calculateAngle(middle.getPosition(), endpoint, referencePoint));
        double clampedAngle = Math.min(middle.angleSize, angle);

        if (clampedAngle == angle) return endpoint;

        double angleDelta = clampedAngle - angle;

        return MathUtil.rotatePointOnAPlaneAround(endpoint, middle.getPosition(), angleDelta, this.getLegPlane());
    }
}
