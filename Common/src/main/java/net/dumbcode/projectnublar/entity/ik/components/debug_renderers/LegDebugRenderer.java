package net.dumbcode.projectnublar.entity.ik.components.debug_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LegDebugRenderer<E extends IKAnimatable<E>, C extends EntityLeg> extends IKChainDebugRenderer<E, IKLegComponent<C, E>> {
    @Override
    public void renderDebug(IKLegComponent<C, E> component, E animatable, PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.renderDebug(component, animatable, poseStack, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        for (C limb : component.getLimbs()) {
            if (!(animatable instanceof Entity entity)) {
                return;
            }

            Vec3 entityPos = entity.position();

            renderLeg(poseStack, bufferSource, limb, entity);

            for (ServerLimb endPoint : component.getEndPoints()) {

                Vec3 limbOffset = endPoint.baseOffset.scale(component.getScale());

                if (component.getStillStandCounter() != component.getSettings().standStillCounter()) {
                    limbOffset = limbOffset.add(0, 0, component.getSettings().stepInFront() * component.getScale());
                }

                limbOffset = limbOffset.yRot((float) Math.toRadians(-entity.getYRot()));

                Vec3 rotatedLimbOffset = limbOffset.add(entity.position());

                BlockHitResult rayCastResult = IKLegComponent.rayCastToGround(rotatedLimbOffset, entity, ClipContext.Fluid.NONE);

                Vec3 rayCastHitPos = rayCastResult.getLocation();

                double distance = endPoint.target.distanceTo(rayCastHitPos);

                if (distance < 0.1) distance = 0;

                if (distance != 0) {
                    IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, endPoint.getPos(), endPoint.target, 255, 100, 255, 127);
                }

                IKDebugRenderer.drawBox(poseStack, bufferSource, endPoint.getPos(), entity, endPoint.isGrounded() ? 0 : 255, endPoint.isGrounded() ? 255 : 0, 0, 127);
                IKDebugRenderer.drawBox(poseStack, bufferSource, endPoint.oldTarget, entity, 0, 255, 255, 127);
                IKDebugRenderer.drawBox(poseStack, bufferSource, rayCastHitPos, entity, 0, 0, 255, 127);
            }
        }
    }

    private void renderLeg(PoseStack poseStack, MultiBufferSource bufferSource, C chain, Entity entity) {
        Vec3 entityPos = entity.position();
        if (chain.entity == null) {
            return;
        }

        drawAngleConstraintsForBase(chain, entity, poseStack, bufferSource);

        this.drawAngleConstraints(chain, entity, poseStack, bufferSource);


        if (chain instanceof EntityLegWithFoot entityLegWithFoot) {
            Vec3 footPos = entityLegWithFoot.foot.getPosition();
            IKDebugRenderer.drawLineToBox(poseStack, bufferSource, entityPos, chain.endJoint, footPos, entity, 255, 165, 0, 127);

            Vec3 angleConstraint = entityLegWithFoot.getFootPosition(entityLegWithFoot.foot.angleSize);

            Vec3 referencePoint = entityLegWithFoot.getFootPosition(0);

            IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, chain.endJoint, angleConstraint, 255, 0, 0, 127);
            IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, chain.endJoint, referencePoint, 0, 255, 0, 127);
        }
    }

    private void drawAngleConstraintsForBase(C chain, Entity entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.position();

        Vec3 base = chain.getFirst().getPosition();

        Vec3 referencePoint = chain.rotatePointOnLegPlane(base.add(chain.getDownNormalOnLegPlane()), base, chain.getFirst().angleOffset);

        Vec3 rotatedPos = chain.rotatePointOnLegPlane(referencePoint, chain.getFirst().getPosition(), chain.getFirst().angleSize);
        Vec3 rotatedPos2 = chain.rotatePointOnLegPlane(referencePoint, chain.getFirst().getPosition(), -chain.getFirst().angleSize);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), rotatedPos, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), rotatedPos2, 0, 255, 0, 127);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), chain.getFirst().getPosition().add(chain.getDownNormalOnLegPlane()), 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), chain.getFirst().getPosition().add(chain.getLegPlane()), 12, 12, 12, 127);
    }

    private void drawAngleConstraints(C chain, Entity entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.position();

        for (int i = 1; i < chain.segments.size() - 1; i++) {
            Segment currentSegment = chain.get(i);
            Segment nextSegment = chain.get(i + 1);

            Vec3 targetDir = nextSegment.getPosition().subtract(currentSegment.getPosition()).normalize();
            Vec3 newPos = currentSegment.getPosition().add(targetDir.scale(currentSegment.length));

            Vec3 referencePoint = chain.rotatePointOnLegPlane(chain.get(i - 1).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);

            Vec3 dotBaseDir = referencePoint.subtract(currentSegment.getPosition()).normalize();
            Vec3 dotTargetDir = newPos.subtract(currentSegment.getPosition()).normalize();

            double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

            double angleDifference = angle - currentSegment.angleSize;

            Vec3 rotationAxis = MathUtil.getUpDirection(currentSegment.getPosition(), newPos, referencePoint);

            newPos = MathUtil.rotatePointOnAPlaneAround(newPos, currentSegment.getPosition(), angleDifference, nextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());
            Vec3 endNewPos1 = MathUtil.rotatePointOnAPlaneAround(newPos, currentSegment.getPosition(), angleDifference + nextSegment.angleSize * 2, nextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());
            Vec3 center = MathUtil.rotatePointOnAPlaneAround(newPos, currentSegment.getPosition(), angleDifference + nextSegment.angleSize, nextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());

            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), center, 255, 0, 100, 127);
            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), newPos, 0, 255, 0, 127);
            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), endNewPos1, 255, 0, 0, 127);
        }

        Segment previouseSegment = chain.get(chain.segments.size() - 2);
        Segment currentSegment = chain.getLast();

        Vec3 referencePoint = chain.rotatePointOnLegPlane(previouseSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), referencePoint, 255, 0, 100, 127);

        Vec3 rotationAxis = MathUtil.getUpDirection(currentSegment.getPosition(), previouseSegment.getPosition(), referencePoint);

        Vec3 rotatedPos = MathUtil.rotatePointOnAPlaneAround(referencePoint, currentSegment.getPosition(), -currentSegment.angleSize, currentSegment.angleOffset > 0 ? rotationAxis.reverse() : rotationAxis);
        Vec3 rotatedPos1 = MathUtil.rotatePointOnAPlaneAround(referencePoint, currentSegment.getPosition(), currentSegment.angleSize, currentSegment.angleOffset > 0 ? rotationAxis.reverse() : rotationAxis);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), rotatedPos, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), rotatedPos1, 0, 255, 0, 127);
        /*
        Vec3 entityPos = entity.position();

        Vec3 endCurrentSegment = chain.endJoint;
        Segment endNextSegment = chain.getLast();

        Vec3 endTargetDir = endNextSegment.getPosition().subtract(endCurrentSegment).normalize();
        Vec3 endNewPos = endCurrentSegment.add(endTargetDir.scale(endNextSegment.length));

        Vec3 referencePoint = chain.rotatePointOnLegPlane(chain.get(chain.segments.size() - 2).getPosition(), endCurrentSegment, endNextSegment.angleOffset);

        Vec3 dotBaseDir = referencePoint.subtract(endCurrentSegment).normalize();
        Vec3 dotTargetDir = endNewPos.subtract(endCurrentSegment).normalize();

        double angle = Math.toDegrees(Math.acos(dotBaseDir.dot(dotTargetDir)));

        double angleDifference = angle - endNextSegment.angleSize;

        Vec3 rotationAxis = MathUtil.getUpDirection(endCurrentSegment, endNewPos, referencePoint);

        endNewPos = MathUtil.rotatePointOnAPlaneAround(endNewPos, endCurrentSegment, angleDifference, endNextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());
        Vec3 endNewPos1 = MathUtil.rotatePointOnAPlaneAround(endNewPos, endCurrentSegment, angleDifference + endNextSegment.angleSize * 2, endNextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());
        Vec3 center = MathUtil.rotatePointOnAPlaneAround(endNewPos, endCurrentSegment, angleDifference + endNextSegment.angleSize, endNextSegment.angleOffset > 0 ? rotationAxis : rotationAxis.reverse());

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, endCurrentSegment, center, 255, 0, 100, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, endCurrentSegment, endNewPos, 0, 255, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, endCurrentSegment, endNewPos1, 255, 0, 0, 127);

         */
    }
}