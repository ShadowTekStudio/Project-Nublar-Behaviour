package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import joptsimple.BuiltinHelpFormatter;
import net.dumbcode.projectnublar.entity.ik.components.debug_renderers.IKTailDebugRenderer;
import net.dumbcode.projectnublar.entity.ik.model.BoneAccessor;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.WorldCollidingSegment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.StretchingIKChain;
import net.dumbcode.projectnublar.entity.ik.util.PrAnCommonClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class IKTailComponent<C extends IKChain, E extends IKAnimatable<E>> extends IKChainComponent<C, E> {
    public Vec3 tailTarget = Vec3.ZERO;
    public Vec3 centerDirection = Vec3.ZERO;
    public Vec3 tailBasePosition = Vec3.ZERO;
    public Vec3 tailBaseRotation = Vec3.ZERO;

    private boolean isReady = false;

    public IKTailComponent(C limb) {
        this.limbs.add(limb);
    }

    @Override
    C setLimb(int index, Vec3 base, Entity entity) {
        this.limbs.get(index).solve(this.tailTarget, base);

        return this.limbs.get(index);
    }

    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void tickServer(E animatable) {

    }

    public void initializeTail(Entity entity) {
        Vec3 newPos = entity.position().add(0, 1, 0);

        this.tailTarget = newPos;
        this.tailBasePosition = newPos;

        for (C chain : this.limbs) {
            if (chain instanceof IKTailComponent.TailStretchingIKChain tailStretchingIKChain) {
                tailStretchingIKChain.tailComponent = this;
            }

            for (Segment segment : chain.segments) {
                if (segment instanceof WorldCollidingSegment worldCollidingSegment && worldCollidingSegment.getLevel() == null) {
                    worldCollidingSegment.setup(entity.level(), entity.position());
                }
            }
        }

        this.isReady = true;
    }

    @Override
    public void tickClient(E animatable, ModelAccessor model) {
        if (!(animatable instanceof Entity entity)) {
            return;
        }

        if (!this.isReady()) {
            this.initializeTail(entity);
        }

        if (Objects.equals(this.tailTarget, new Vec3(0, 0, 0))) {
            if (model.getBone("tail1_base").isEmpty()) {
                return;
            }
            this.tailTarget = model.getBone("tail1_base").get().getPosition();
        }

        double dot = (this.tailBaseRotation.dot(this.centerDirection.normalize()) + 1) / 2;

        Vec3 newPos = this.tailBasePosition.add(this.centerDirection.scale(this.getLimb().getMaxLength() * dot));

        this.tailTarget = this.getMovedTailPos(newPos, entity);

        this.setLimb(0, this.tailBasePosition, entity);

        for (int i = 0; i < this.limbs.get(0).getJoints().size() - 1; i++) {
            Segment currentSegment = this.getLimb().segments.get(i);

            if (model.getBone("segment" + (i + 1) + "_tail" + 1).isEmpty()) {
                return;
            }
            BoneAccessor bone = model.getBone("segment" + (i + 1) + "_tail" + 1).get();

            Vec3 endPos = this.getLimb().getJoints().get(i + 1);

            if (PrAnCommonClass.shouldRenderDebugLegs) {
                bone.moveTo(currentSegment.getPosition().subtract(0, 200, 0), endPos.subtract(0, 200, 0), entity);
                continue;
            }

            bone.moveTo(currentSegment.getPosition(), endPos, entity);
        }
    }

    @Override
    public void getModelPositions(E animatable, ModelAccessor model) {
        if (!(animatable instanceof Entity entity)) {
            return;
        }

        if (model.getBone("tail1_base").isEmpty()) {
            return;
        }

        this.tailBasePosition = model.getBone("tail1_base").get().getPosition();
        this.tailBaseRotation = model.getBone("tail1_base").get().getRotationVec().normalize().yRot((float) -Math.toRadians(entity.getYRot()));

        if (model.getBone("center_of_mass").isEmpty()) {
            return;
        }

        if (model.getBone("head").isEmpty()) {
            return;
        }

        this.centerDirection = model.getBone("center_of_mass").get().getPosition().subtract(model.getBone("head").get().getPosition()).normalize();
    }

    private Vec3 getMovedTailPos(Vec3 newPos, Entity entity) {
        Vec3 collisionPoint = entity.level().clip(new ClipContext(
                this.tailTarget,
                newPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                new Arrow(entity.level(), newPos.x(), newPos.y(), newPos.z())
        )).getLocation();

        if (collisionPoint != newPos) {
            collisionPoint = this.tailTarget;
        }
        return collisionPoint;
    }

    private C getLimb() {
        return this.limbs.get(0);
    }

    @Override
    public void renderDebug(PoseStack poseStack, E animatable, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        new IKTailDebugRenderer<E>().renderDebug(this, animatable, poseStack, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    public static class TailStretchingIKChain extends StretchingIKChain {
        public IKTailComponent<?, ?> tailComponent;

        public TailStretchingIKChain(double... lengths) {
            super(lengths);
        }

        public TailStretchingIKChain(Segment... segments) {
            super(segments);
        }

        @Override
        public Vec3 getStretchingPos(Vec3 target, Vec3 base) {
            if (tailComponent == null) {
                return StretchingIKChain.stretchToTargetPos(target, this);
            }

            return StretchingIKChain.stretchToTargetPos(tailComponent.tailBasePosition.add(tailComponent.tailBaseRotation.scale(this.getMaxLength())), this);
        }
    }
}
