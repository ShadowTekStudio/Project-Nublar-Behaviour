package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.components.debug_renderers.IKTailDebugRenderer;
import net.dumbcode.projectnublar.entity.ik.model.BoneAccessor;
import net.dumbcode.projectnublar.entity.ik.model.EntityAccessor;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.WorldCollidingSegment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.StretchingIKChain;
import net.dumbcode.projectnublar.entity.ik.util.PrAnCommonClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
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
    C setLimb(int index, Vec3 base, EntityAccessor entity) {
        this.limbs.get(index).solve(this.tailTarget, base);

        return this.limbs.get(index);
    }

    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void tickServer(E animatable) {

    }

    public void initializeTail(EntityAccessor entity) {
        Vec3 newPos = entity.getPosition().add(0, 1, 0);

        this.tailTarget = newPos;
        this.tailBasePosition = newPos;

        for (C chain : this.limbs) {
            if (chain instanceof IKTailComponent.TailStretchingIKChain tailStretchingIKChain) {
                tailStretchingIKChain.tailComponent = this;
            }

            for (Segment segment : chain.segments) {
                if (segment instanceof WorldCollidingSegment worldCollidingSegment) {
                    worldCollidingSegment.setup(entity.getLevel(), newPos);
                }
            }
        }

        this.isReady = true;
    }

    @Override
    public void tickClient(E animatable, ModelAccessor model) {
        if (!this.isReady()) {
            this.initializeTail(animatable.getAccessor());
        }

        if (Objects.equals(this.tailTarget, new Vec3(0, 0, 0))) {
            if (model.getBone("tail1_base").isEmpty()) {
                return;
            }
            this.tailTarget = model.getBone("tail1_base").get().getPosition();
        }

        double dot = (this.tailBaseRotation.dot(this.centerDirection.normalize()) + 1) / 2;

        Vec3 newPos = this.tailBasePosition.add(this.centerDirection.scale(this.getLimb().getMaxLength() * dot));

        this.tailTarget = this.getMovedTailPos(newPos, animatable.getAccessor());

        this.setLimb(0, this.tailBasePosition, animatable.getAccessor());

        if (this.tailBasePosition.distanceToSqr(this.limbs.get(0).getFirst().getPosition()) > 2) {
            this.isReady = false;
        }

        for (int i = 0; i < this.limbs.get(0).getJoints().size() - 1; i++) {
            Segment currentSegment = this.getLimb().segments.get(i);

            if (model.getBone("segment" + (i + 1) + "_tail" + 1).isEmpty()) {
                return;
            }
            BoneAccessor bone = model.getBone("segment" + (i + 1) + "_tail" + 1).get();

            Vec3 endPos = this.getLimb().getJoints().get(i + 1);

            if (PrAnCommonClass.shouldRenderDebugLegs) {
                bone.moveTo(currentSegment.getPosition().subtract(0, 200, 0), endPos.subtract(0, 200, 0), animatable.getAccessor());
                continue;
            }

            bone.moveTo(currentSegment.getPosition(), endPos, animatable.getAccessor());
        }
    }

    @Override
    public void getModelPositions(E animatable, ModelAccessor model) {
        if (model.getBone("tail1_base").isEmpty()) {
            return;
        }

        this.tailBasePosition = model.getBone("tail1_base").get().getPosition();
        this.tailBaseRotation = model.getBone("tail1_base").get().getRotationVec().normalize().yRot((float) -Math.toRadians(animatable.getAccessor().getYRot()));

        if (model.getBone("center_of_mass").isEmpty()) {
            return;
        }

        if (model.getBone("head").isEmpty()) {
            return;
        }

        this.centerDirection = model.getBone("center_of_mass").get().getPosition().subtract(model.getBone("head").get().getPosition()).normalize();
    }

    private Vec3 getMovedTailPos(Vec3 newPos, EntityAccessor entity) {
        Vec3 collisionPoint = this.tailTarget;

        BlockHitResult blockCollisionPoint = entity.getLevel().clip(new ClipContext(
                this.tailTarget,
                newPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                new Arrow(entity.getLevel(), newPos.x(), newPos.y(), newPos.z())
        ));

        Vec3 direction = blockCollisionPoint.getLocation().subtract(collisionPoint).normalize();

        collisionPoint = blockCollisionPoint.getLocation().subtract(direction.scale(0.01));

        /*
        BlockHitResult blockCollisionPoint2 = entity.level().clip(new ClipContext(
                this.getLimb().getLast().getPosition(),
                collisionPoint,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                new Arrow(entity.level(), newPos.x(), newPos.y(), newPos.z())
        ));

        if (blockCollisionPoint2.getType() == BlockHitResult.Type.MISS) {
            collisionPoint = blockCollisionPoint2.getLocation();
        }
         */

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
            throw new UnsupportedOperationException("This constructor is not supported for TailStretchingIKChain");
        }

        public TailStretchingIKChain(WorldCollidingSegment... segments) {
            super(segments);
        }

        @Override
        public Vec3 getStretchingPos(Vec3 target, Vec3 base) {
            if (tailComponent == null) {
                return StretchingIKChain.stretchToTargetPos(target, this);
            }

            return StretchingIKChain.stretchToTargetPos(tailComponent.tailBasePosition.add(tailComponent.tailBaseRotation.scale(this.getMaxLength())), this);
        }

        /*
        @Override
        public void reachForwards(Vec3 target) {
            this.endJoint = target;

            ((WorldCollidingSegment) this.getLast()).move(this.moveSegment(this.getLast().getPosition(), this.endJoint, this.getLast().length), endJoint);
            for (int i = this.segments.size() - 1; i > 0; i--) {
                Segment currentSegment = this.segments.get(i);
                Segment nextSegment = this.segments.get(i - 1);

                ((WorldCollidingSegment) nextSegment).move(this.moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), nextSegment.length), currentSegment.getPosition());
            }
        }

        @Override
        public void reachBackwards(Vec3 base) {
            this.getFirst().move(base);

            for (int i = 0; i < this.segments.size() - 1; i++) {
                Segment currentSegment = this.segments.get(i);
                Segment nextSegment = this.segments.get(i + 1);

                ((WorldCollidingSegment) nextSegment).move(this.moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), currentSegment.length), currentSegment.getPosition());
            }

            WorldCollidingSegment endSegment = new WorldCollidingSegment(new Segment.Builder());
            endSegment.setup(((WorldCollidingSegment) this.getFirst()).getLevel(), this.getFirst().getPosition());

            endSegment.move(this.moveSegment(this.endJoint, this.getLast().getPosition(), this.getLast().length), this.getLast().getPosition());

            this.endJoint = endSegment.getPosition();
        }
        */
    }
}
