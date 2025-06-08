package net.dumbcode.projectnublar.entity.ik.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.entity.ik.components.debug_renderers.LegDebugRenderer;
import net.dumbcode.projectnublar.entity.ik.model.BoneAccessor;
import net.dumbcode.projectnublar.entity.ik.model.EntityAccessor;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLeg;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.entity.ik.util.ChainCommonClass;
import net.dumbcode.projectnublar.entity.ik.util.MathUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IKLegComponent<C extends EntityLeg, E extends IKAnimatable<E>> extends IKChainComponent<C, E> {
    /// summon projectnublar:tyrannosaurus_rex ~ ~ ~ {NoAI:1b}
    private final List<ServerLimb> endPoints;
    private List<Vec3> bases;
    private final LegSetting settings;
    public double scale = 1;
    private int stillStandCounter = 0;

    @SafeVarargs
    public IKLegComponent(LegSetting settings, List<ServerLimb> endpoints, C... limbs) {
        this.limbs.addAll(List.of(limbs));
        this.settings = settings;
        this.endPoints = endpoints;
        this.bases = new ArrayList<>();
        Arrays.stream(limbs).forEach(
                limb -> this.bases.add(new Vec3(0,0,0))
        );
    }

    public static boolean hasMovedOverLastTick(EntityAccessor entity) {
        return !entity.getPosition().equals(entity.getOldPosition());
    }

    public static BlockHitResult rayCastToGround(Vec3 rotatedLimbOffset, EntityAccessor entity, ClipContext.Fluid fluid) {
        Level world = entity.getLevel();
        return world.clip(new ClipContext(rotatedLimbOffset.relative(Direction.UP, 3), rotatedLimbOffset.relative(Direction.DOWN, 10), ClipContext.Block.COLLIDER, fluid, new Arrow(entity.getLevel(), entity.getPosition().x, entity.getPosition().y, entity.getPosition().z)));
    }

    @Override
    public void tickClient(E animatable, ModelAccessor model) {
        double sum = 0;

        for (Vec3 point : this.endPoints.stream().map((serverLimb -> serverLimb.target)).toList()) {
            sum += point.y;
        }

        double average = sum / this.endPoints.size();

        for (int i = 0; i < this.limbs.size(); i++) {
            if (model.getBone("base_" + "leg" + (i + 1)).isEmpty()) {
                return;
            }
            //BoneAccessor baseAccessor = model.getBone("base_" + "leg" + (i + 1)).get();//

            //Vec3 basePosWorldSpace = baseAccessor.getPosition();
            if (this.bases.isEmpty()) {
                return;
            }

            Vec3 basePosWorldSpace = this.bases.get(i);

            C limb = this.setLimb(i, basePosWorldSpace, animatable.getAccessor());

            for (int k = 0; k < limb.getJoints().size() - 1; k++) {
                Vec3 modelPosWorldSpace = limb.getJoints().get(k);
                Vec3 targetVecWorldSpace = limb.getJoints().get(k + 1);

                if (model.getBone("segment" + (k + 1) + "_leg" + (i + 1)).isEmpty()) {
                    return;
                }
                BoneAccessor legSegmentAccessor = model.getBone("segment" + (k + 1) + "_leg" + (i + 1)).get();

                if (ChainCommonClass.shouldRenderDebugLegs) {
                    modelPosWorldSpace = modelPosWorldSpace.subtract(0, 200, 0);
                    targetVecWorldSpace = targetVecWorldSpace.subtract(0, 200, 0);
                }

                legSegmentAccessor.moveTo(modelPosWorldSpace, targetVecWorldSpace, animatable.getAccessor());

                if (limb instanceof EntityLegWithFoot entityLegWithFoot) {
                    if (model.getBone("foot_leg" + (i + 1)).isEmpty()) {
                        return;
                    }
                    BoneAccessor footSegmentAccessor = model.getBone("foot_leg" + (i + 1)).get();

                    Vec3 shortenedEndPoint = limb.getLast().getPosition().add(limb.endJoint.subtract(limb.getLast().getPosition()).normalize().scale(limb.getLast().length * 0.8));

                    double yOffset = shortenedEndPoint.subtract(limb.endJoint).y;

                    footSegmentAccessor.moveTo(ChainCommonClass.shouldRenderDebugLegs ? shortenedEndPoint.subtract(0, 200, 0) : shortenedEndPoint, entityLegWithFoot.getFootPosition().add(0, yOffset, 0), animatable.getAccessor());
                }
            }
        }
    }

    @Override
    public void getModelPositions(E animatable, ModelAccessor model) {
        for (int i = 0; i < this.limbs.size(); i++) {
            if (model.getBone("base_" + "leg" + (i + 1)).isEmpty()) {
                return;
            }

            BoneAccessor baseAccessor = model.getBone("base_" + "leg" + (i + 1)).get();

            Vec3 basePosWorldSpace = baseAccessor.getPosition();

            this.bases.set(i, basePosWorldSpace);
        }
    }

    @Override
    public void tickServer(E animatable) {
        this.setScale(animatable.getSize());

        EntityAccessor entity = animatable.getAccessor();

        Vec3 pos = entity.getPosition();

        for (int i = 0; i < this.endPoints.size(); i++) {
            ServerLimb limb = this.endPoints.get(i);

            limb.tick(this, i);

            Vec3 limbOffset = limb.baseOffset.scale(this.getScale());

            limbOffset = limbOffset.yRot((float) Math.toRadians(-entity.getYRot()));

            if (hasMovedOverLastTick(entity)) {
                Vec3 movementDir = MathUtil.convertToFlatVector(entity.getPosition().subtract(entity.getOldPosition()));

                double forwardMoveness = movementDir.normalize().dot(entity.getForwardFacingVector().normalize());

                //if (forwardMoveness > 0.25) {
                    limbOffset = limbOffset.add(MathUtil.convertToFlatVector(entity.getForwardFacingVector()).normalize().scale((float) this.settings.stepInFront()));
                //}
            }


            Vec3 rotatedLimbOffset = limbOffset.add(pos);

            BlockHitResult rayCastResult = rayCastToGround(rotatedLimbOffset, entity, this.settings.fluid());

            BlockHitResult rayCastResultXFloor = rayCastToGround(new Vec3(Math.floor(rotatedLimbOffset.x), rotatedLimbOffset.y, rotatedLimbOffset.z), entity, this.settings.fluid());
            BlockHitResult rayCastResultXCeiling = rayCastToGround(new Vec3(Math.ceil(rotatedLimbOffset.x), rotatedLimbOffset.y, rotatedLimbOffset.z), entity, this.settings.fluid());
            BlockHitResult rayCastResultZFloor = rayCastToGround(new Vec3(rotatedLimbOffset.x, rotatedLimbOffset.y, Math.floor(rotatedLimbOffset.z)), entity, this.settings.fluid());
            BlockHitResult rayCastResultZCeiling = rayCastToGround(new Vec3(rotatedLimbOffset.x, rotatedLimbOffset.y, Math.ceil(rotatedLimbOffset.z)), entity, this.settings.fluid());

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

            if (limb.hasToBeSet) {
                limb.set(rayCastHitPos);
            }

            Vec3 baseLimbOffset = limb.baseOffset.scale(this.getScale());

            baseLimbOffset = baseLimbOffset.yRot((float) Math.toRadians(-entity.getYRot())).add(pos);

            if (limb.pos.distanceTo(rayCastToGround(baseLimbOffset, entity, ClipContext.Fluid.NONE).getLocation()) > this.getMaxLegFormTargetDistance(entity)) {
                limb.setTarget(rayCastHitPos);
            }
        }
    }

    @Override
    public void renderDebug(PoseStack poseStack, E animatable, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        new LegDebugRenderer<E, C>().renderDebug(this, animatable, poseStack, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    private double getMaxLegFormTargetDistance(EntityAccessor entity) {
        if (this.stillStandCounter >= this.settings.standStillCounter() || hasMovedOverLastTick(entity)) {
            this.stillStandCounter = 0;
        } else if (this.stillStandCounter < this.settings.standStillCounter()) {
            this.stillStandCounter += 1;
        }

        if (this.stillStandCounter == this.settings.standStillCounter()) {
            return this.settings.maxStandingStillDistance() * this.getScale();
        } else {
            return this.settings.maxDistance() * this.getScale();
        }
    }

    public List<C> getLimbs() {
        return this.limbs;
    }

    public List<ServerLimb> getEndPoints() {
        return this.endPoints;
    }

    public LegSetting getSettings() {
        return this.settings;
    }

    public int getStillStandCounter() {
        return this.stillStandCounter;
    }

    @Override
    public C setLimb(int index, Vec3 base, EntityAccessor entity) {
        C limb = this.limbs.get(index);

        limb.entity = entity;


        limb.setScale(this.getScale());

        limb.solve(this.endPoints.get(index).getPos(), base);

        return limb;
    }

    public double getScale() {
        return this.scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public static class LegSetting {
        private final ClipContext.Fluid fluid;
        private final double maxStandingStillDistance;
        private final double maxDistance;
        private final double stepInFront;
        private final double movementSpeed;
        private final int standStillCounter;
        private final double steppingParabolaStrength;

        private LegSetting(ClipContext.Fluid fluid, double maxStandingStillDistance, double maxDistance, double stepInFront, double movementSpeed, int standStillCounter, double steppingParabolaStrength) {
            this.fluid = fluid;
            this.maxStandingStillDistance = maxStandingStillDistance;
            this.maxDistance = maxDistance;
            this.stepInFront = stepInFront;
            this.movementSpeed = movementSpeed;
            this.standStillCounter = standStillCounter;
            this.steppingParabolaStrength = steppingParabolaStrength;
        }

        public ClipContext.Fluid fluid() {
            return this.fluid;
        }

        public double maxStandingStillDistance() {
            return this.maxStandingStillDistance;
        }

        public double maxDistance() {
            return this.maxDistance;
        }

        public double stepInFront() {
            return this.stepInFront;
        }

        public double movementSpeed() {
            return this.movementSpeed;
        }

        public int standStillCounter() {
            return this.standStillCounter;
        }

        public double steppingParabolaStrength() {
            return this.steppingParabolaStrength;
        }

        public static class Builder {
            private ClipContext.Fluid fluid = ClipContext.Fluid.NONE;
            private double maxStandingStillDistance = 0.1;
            private double maxDistance = 1;
            private double stepInFront = 1;
            private double movementSpeed = 0.2;
            private int standStillCounter = 20;
            private double steppingParabolaStrength = 2;

            public Builder() {
            }

            public LegSetting.Builder fluid(ClipContext.Fluid fluid) {
                this.fluid = fluid;
                return this;
            }

            public LegSetting.Builder maxStandingStillDistance(double maxStandingStillDistance) {
                this.maxStandingStillDistance = maxStandingStillDistance;
                return this;
            }

            public LegSetting.Builder maxDistance(double maxDistance) {
                this.maxDistance = maxDistance;
                return this;
            }

            public LegSetting.Builder steppingParabolaStrength(double steppingParabolaStrength) {
                this.steppingParabolaStrength = steppingParabolaStrength;
                return this;
            }

            public LegSetting.Builder standStillCounter(int standStillCounter) {
                this.standStillCounter = standStillCounter;
                return this;
            }

            public LegSetting.Builder stepInFront(double stepInFront) {
                this.stepInFront = stepInFront;
                return this;
            }

            public LegSetting.Builder movementSpeed(double movementSpeed) {
                this.movementSpeed = movementSpeed;
                return this;
            }

            public LegSetting build() {
                return new LegSetting(fluid, maxStandingStillDistance, maxDistance, stepInFront, movementSpeed, standStillCounter, steppingParabolaStrength);
            }
        }
    }
}