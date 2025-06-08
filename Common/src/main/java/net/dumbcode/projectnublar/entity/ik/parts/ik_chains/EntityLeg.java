package net.dumbcode.projectnublar.entity.ik.parts.ik_chains;

import net.dumbcode.projectnublar.entity.ik.model.EntityAccessor;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityLeg extends AngleConstraintIKChain {
    public EntityAccessor entity;

    public EntityLeg(double floorAngle, double... lengths) {
        super(floorAngle, lengths);
    }

    public EntityLeg(double floorAngle, Segment... segments) {
        super(floorAngle, segments);
    }


    @Override
    public Vec3 getReferencePoint() {
        Vec3 referencePoint = MathUtil.getFlatRotationVector(this.entity.getYRot() + 90);
        return this.getFirst().getPosition().add(referencePoint.scale(100));
    }

    @Override
    public Vec3 getStretchingPos(Vec3 target, Vec3 base) {
        //return base.add(target.add(MathUtil.getFlatRotationVector(this.entity.getYRot()).scale(2)).subtract(base).scale(this.getMaxLength()))/*.add(this.getDownNormalOnLegPlane().scale(5))*/;
        return base.add(MathUtil.getFlatRotationVector(this.entity.getYRot()).scale(this.getMaxLength()))/*.add(this.getDownNormalOnLegPlane(target, base).scale(5))*/;
    }

    public Vec3 getDownNormalOnLegPlane() {
        Vec3 baseRotated = this.getFirst().getPosition().yRot((float) Math.toRadians(this.entity.getYRot()));
        Vec3 targetRotated = this.endJoint.yRot((float) Math.toRadians(this.entity.getYRot()));

        Vec3 flatRotatedBase = new Vec3(baseRotated.x(), baseRotated.y(), 0);
        Vec3 flatRotatedTarget = new Vec3(targetRotated.x(), targetRotated.y(), 0);

        Vec3 flatBase = flatRotatedBase.yRot((float) Math.toRadians(-this.entity.getYRot()));
        Vec3 flatTarget = flatRotatedTarget.yRot((float) Math.toRadians(-this.entity.getYRot()));

        return flatTarget.subtract(flatBase).normalize();
    }

    public Vec3 getLegNormal(Vec3 target, Vec3 base) {
        return MathUtil.getNormalClosestTo(base, target, base.add(MathUtil.getFlatRotationVector(this.entity.getYRot()).scale(this.getMaxLength() * 2)), this.getReferencePoint());
    }
}
