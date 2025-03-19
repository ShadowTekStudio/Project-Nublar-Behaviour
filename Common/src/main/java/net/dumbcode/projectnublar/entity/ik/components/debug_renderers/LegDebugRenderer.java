package net.dumbcode.projectnublar.entity.ik.components.debug_renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LegDebugRenderer<E extends IKAnimatable<E>, C extends EntityLeg> extends IKChainDebugRenderer<E, IKLegComponent<C, E>> {
    @Override
    public void renderDebug(IKLegComponent<C, E> component, E animatable, PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.renderDebug(component, animatable, poseStack, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        if (!(animatable instanceof PathfinderMob entity)) {
            return;
        }

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

        for (C limb : component.getLimbs()) {
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