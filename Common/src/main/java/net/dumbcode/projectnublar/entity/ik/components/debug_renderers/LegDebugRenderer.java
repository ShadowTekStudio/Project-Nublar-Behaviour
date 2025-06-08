package net.dumbcode.projectnublar.entity.ik.components.debug_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.model.EntityAccessor;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LegDebugRenderer<E extends IKAnimatable<E>, C extends EntityLeg> extends IKChainDebugRenderer<E, IKLegComponent<C, E>> {
    @Override
    public void renderDebug(IKLegComponent<C, E> component, E animatable, PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.renderDebug(component, animatable, poseStack, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        /*
        Vec3 pos = entity.position();

        for (int i = 0; i < component.getEndPoints().size(); i++) {
            ServerLimb limb = component.getEndPoints().get(i);

            Vec3 limbOffset = limb.baseOffset.scale(component.getScale());

            limbOffset = limbOffset.yRot((float) Math.toRadians(-entity.getYRot()));

            Vec3 rotatedLimbOffset = limbOffset.add(pos);

            List<Vec3> hitPosses = List.of(
                    IKLegComponent.rayCastToGround(new Vec3(Math.floor(rotatedLimbOffset.x) - .1, rotatedLimbOffset.y, rotatedLimbOffset.z), entity, component.getSettings().fluid()).getLocation(),
                    IKLegComponent.rayCastToGround(new Vec3(Math.floor(rotatedLimbOffset.x) - .1, rotatedLimbOffset.y, Math.floor(rotatedLimbOffset.z) - .1), entity, component.getSettings().fluid()).getLocation(),
                    IKLegComponent.rayCastToGround(new Vec3(Math.floor(rotatedLimbOffset.x) - .1, rotatedLimbOffset.y, Math.ceil(rotatedLimbOffset.z) + .1), entity, component.getSettings().fluid()).getLocation(),

                    IKLegComponent.rayCastToGround(new Vec3(Math.ceil(rotatedLimbOffset.x) + .1, rotatedLimbOffset.y, rotatedLimbOffset.z), entity, component.getSettings().fluid()).getLocation(),
                    IKLegComponent.rayCastToGround(new Vec3(Math.ceil(rotatedLimbOffset.x) + .1, rotatedLimbOffset.y, Math.floor(rotatedLimbOffset.z) - .1), entity, component.getSettings().fluid()).getLocation(),
                    IKLegComponent.rayCastToGround(new Vec3(Math.ceil(rotatedLimbOffset.x) + .1, rotatedLimbOffset.y, Math.ceil(rotatedLimbOffset.z) + .1), entity, component.getSettings().fluid()).getLocation(),
                    IKLegComponent.rayCastToGround(new Vec3(rotatedLimbOffset.x, rotatedLimbOffset.y, Math.floor(rotatedLimbOffset.z) - .1), entity, component.getSettings().fluid()).getLocation(),
                    IKLegComponent.rayCastToGround(new Vec3(rotatedLimbOffset.x, rotatedLimbOffset.y, Math.ceil(rotatedLimbOffset.z) + .1), entity, component.getSettings().fluid()).getLocation()
            );

            for (Vec3 hitPoss : hitPosses) {
                IKDebugRenderer.drawLine(poseStack, bufferSource, pos, hitPoss, hitPoss.add(0, 10, 0), 255, 100, 255, 127);
            }
        }
         */
        EntityAccessor entity = animatable.getAccessor();

        for (C limb : component.getLimbs()) {
            Vec3 entityPos = entity.getPosition();

            renderLeg(poseStack, bufferSource, limb, entity);

            for (ServerLimb endPoint : component.getEndPoints()) {

                Vec3 limbOffset = endPoint.baseOffset.scale(component.getScale());

                limbOffset = limbOffset.yRot((float) Math.toRadians(-entity.getYRot()));

                if (IKLegComponent.hasMovedOverLastTick(entity)) {
                    Vec3 movementDir = MathUtil.convertToFlatVector(entity.getPosition().subtract(entity.getOldPosition()));

                    double forwardMoveness = movementDir.normalize().dot(entity.getForwardFacingVector().normalize());

                    //if (forwardMoveness > 0.25) {
                    limbOffset = limbOffset.add(MathUtil.convertToFlatVector(entity.getForwardFacingVector()).normalize().scale((float) component.getSettings().stepInFront()));
                    //}
                }


                Vec3 rotatedLimbOffset = limbOffset.add(entityPos);

                BlockHitResult rayCastResult = IKLegComponent.rayCastToGround(rotatedLimbOffset, entity, component.getSettings().fluid());

                BlockHitResult rayCastResultXFloor = IKLegComponent.rayCastToGround(new Vec3(Math.floor(rotatedLimbOffset.x), rotatedLimbOffset.y, rotatedLimbOffset.z), entity, component.getSettings().fluid());
                BlockHitResult rayCastResultXCeiling = IKLegComponent.rayCastToGround(new Vec3(Math.ceil(rotatedLimbOffset.x), rotatedLimbOffset.y, rotatedLimbOffset.z), entity, component.getSettings().fluid());
                BlockHitResult rayCastResultZFloor = IKLegComponent.rayCastToGround(new Vec3(rotatedLimbOffset.x, rotatedLimbOffset.y, Math.floor(rotatedLimbOffset.z)), entity, component.getSettings().fluid());
                BlockHitResult rayCastResultZCeiling = IKLegComponent.rayCastToGround(new Vec3(rotatedLimbOffset.x, rotatedLimbOffset.y, Math.ceil(rotatedLimbOffset.z)), entity, component.getSettings().fluid());

                List<Vec3> hitPosses = List.of(
                        rayCastResultXFloor.getLocation(),
                        rayCastResultXCeiling.getLocation(),
                        rayCastResultZFloor.getLocation(),
                        rayCastResultZCeiling.getLocation()
                );

                Vec3 optimalPos = rayCastResult.getLocation();
                Vec3 restPos = new Vec3(rayCastResult.getLocation().x, rayCastResult.getLocation().y + 0.5, rayCastResult.getLocation().z);

                for (Vec3 hitPoss : hitPosses) {
                    if (hitPoss.distanceToSqr(restPos) < optimalPos.distanceToSqr(restPos)) {
                        optimalPos = hitPoss;
                    }
                }

                Vec3 rayCastHitPos = optimalPos;

                Vec3 baseLimbOffset = endPoint.baseOffset.scale(limb.getScale());

                baseLimbOffset = baseLimbOffset.yRot((float) Math.toRadians(-entity.getYRot())).add(entityPos);

                double distance = endPoint.target.distanceTo(baseLimbOffset);

                if (distance < 0.1) distance = 0;

                if (distance != 0) {
                    IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, endPoint.getPos(), endPoint.target, 255, 100, 255, 127);
                }

                IKDebugRenderer.drawBox(poseStack, bufferSource, endPoint.getPos(), entity, endPoint.isGrounded() ? 0 : 255, endPoint.isGrounded() ? 255 : 0, 0, 127);
                IKDebugRenderer.drawBox(poseStack, bufferSource, endPoint.oldTarget, entity, 0, 255, 255, 127);
                IKDebugRenderer.drawBox(poseStack, bufferSource, baseLimbOffset, entity, 0, 0, 255, 127);
            }
        }
    }

    private void renderLeg(PoseStack poseStack, MultiBufferSource bufferSource, C chain, EntityAccessor entity) {
        Vec3 entityPos = entity.getPosition();
        if (chain.entity == null) {
            return;
        }

        if (IKChain.MAXLECK % 2 == 1) {
            drawBackwardsAngleConstraintsForBase(chain, entity, poseStack, bufferSource);
            drawBackwardsAngleConstraints(chain, entity, poseStack, bufferSource);
        }
        else {
            drawForwardsAngleConstraintsForBase(chain, entity, poseStack, bufferSource);
            drawForwardsAngleConstraints(chain, entity, poseStack, bufferSource);
        }


        if (chain instanceof EntityLegWithFoot entityLegWithFoot) {
            Vec3 footPos = entityLegWithFoot.foot.getPosition();
            IKDebugRenderer.drawLineToBox(poseStack, bufferSource, entityPos, chain.endJoint, footPos, entity, 255, 165, 0, 127);

            Vec3 angleConstraint = entityLegWithFoot.getFootPosition(entityLegWithFoot.foot.angleSize);

            Vec3 referencePoint = entityLegWithFoot.getFootPosition(0);

            IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, chain.endJoint, angleConstraint, 255, 0, 0, 127);
            IKDebugRenderer.drawLine(poseStack, bufferSource, entityPos, chain.endJoint, referencePoint, 0, 255, 0, 127);
        }
    }

    private void drawForwardsAngleConstraintsForBase(C chain, EntityAccessor entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.getPosition();

        Vec3 base = chain.get(1).getPosition();

        Vec3 referencePoint = chain.rotatePointOnLegPlane(base.add(chain.getDownNormalOnLegPlane().reverse()), base, chain.getFirst().angleOffset);

        Vec3 rotatedPos = chain.rotatePointOnLegPlane(referencePoint, base, -chain.getFirst().angleSize);
        Vec3 rotatedPos2 = chain.rotatePointOnLegPlane(referencePoint, base, chain.getFirst().angleSize);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, base, rotatedPos, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, base, rotatedPos2, 0, 255, 0, 127);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, base, referencePoint, 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, base, base.add(chain.getLegPlane()), 12, 12, 12, 127);
    }

    private void drawBackwardsAngleConstraintsForBase(C chain, EntityAccessor entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.getPosition();

        Vec3 base = chain.getFirst().getPosition();

        Vec3 referencePoint = chain.rotatePointOnLegPlane(base.add(chain.getDownNormalOnLegPlane()), base, chain.getFirst().angleOffset);

        Vec3 rotatedPos = chain.rotatePointOnLegPlane(referencePoint, chain.getFirst().getPosition(), chain.getFirst().angleSize);
        Vec3 rotatedPos2 = chain.rotatePointOnLegPlane(referencePoint, chain.getFirst().getPosition(), -chain.getFirst().angleSize);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), rotatedPos, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), rotatedPos2, 0, 255, 0, 127);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), chain.getFirst().getPosition().add(chain.getDownNormalOnLegPlane()), 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.getFirst().getPosition(), chain.getFirst().getPosition().add(chain.getLegPlane()), 12, 12, 12, 127);
    }

    private void drawForwardsAngleConstraints(C chain, EntityAccessor entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.getPosition();

        for (int i = chain.segments.size() - 1; i > 1; i--) {
            Segment currentSegment = chain.get(i);

            Vec3 max = chain.rotatePointOnLegPlane(i == chain.segments.size() - 1 ? chain.endJoint : chain.get(i + 1).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset + currentSegment.angleSize);
            Vec3 middle = chain.rotatePointOnLegPlane(i == chain.segments.size() - 1 ? chain.endJoint : chain.get(i + 1).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);
            Vec3 min = chain.rotatePointOnLegPlane(i == chain.segments.size() - 1 ? chain.endJoint : chain.get(i + 1).getPosition(), currentSegment.getPosition(), currentSegment.angleOffset - currentSegment.angleSize);

            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), max, 255, 0, 0, 127);
            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), middle, 180, 180, 180, 127);
            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), min, 0, 255, 0, 127);
        }

        Vec3 footToBase = chain.getBase().subtract(chain.endJoint).normalize().add(chain.endJoint);

        Vec3 max = chain.rotatePointOnLegPlane(footToBase, chain.endJoint, (90 + chain.getFloorAngle()) + 90);
        Vec3 middle = chain.rotatePointOnLegPlane(footToBase, chain.endJoint, (90 + chain.getFloorAngle()));
        Vec3 min = chain.rotatePointOnLegPlane(footToBase, chain.endJoint, (90 + chain.getFloorAngle()) - 90);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.endJoint, footToBase, 255, 255, 255, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.endJoint, max, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.endJoint, middle, 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, chain.endJoint, min, 0, 255, 0, 127);
    }

    private void drawBackwardsAngleConstraints(C chain, EntityAccessor entity, PoseStack matrices, MultiBufferSource vertexConsumers) {
        Vec3 entityPos = entity.getPosition();

        for (int i = 1; i < chain.segments.size() - 1; i++) {
            Segment previousSegment = chain.get(i - 1);
            Segment currentSegment = chain.segments.get(i);

            Vec3 max = chain.rotatePointOnLegPlane(previousSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset + currentSegment.angleSize);
            Vec3 middle = chain.rotatePointOnLegPlane(previousSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);
            Vec3 min = chain.rotatePointOnLegPlane(previousSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset - currentSegment.angleSize);

            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), max, 255, 0, 0, 127);
            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), middle, 180, 180, 180, 127);
            IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), min, 0, 255, 0, 127);
        }

        Segment previousSegment = chain.get(chain.segments.size() - 2);
        Segment currentSegment = chain.getLast();

        Vec3 max = chain.rotatePointOnLegPlane(previousSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset + currentSegment.angleSize);
        Vec3 middle = chain.rotatePointOnLegPlane(previousSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset);
        Vec3 min = chain.rotatePointOnLegPlane(previousSegment.getPosition(), currentSegment.getPosition(), currentSegment.angleOffset - currentSegment.angleSize);

        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), max, 255, 0, 0, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), middle, 180, 180, 180, 127);
        IKDebugRenderer.drawLine(matrices, vertexConsumers, entityPos, currentSegment.getPosition(), min, 0, 255, 0, 127);
    }
}