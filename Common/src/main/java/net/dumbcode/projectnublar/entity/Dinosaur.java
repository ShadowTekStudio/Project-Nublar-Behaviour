package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.ik.components.IKAnimatable;
import net.dumbcode.projectnublar.entity.ik.components.IKLegComponent;
import net.dumbcode.projectnublar.entity.ik.components.IKModelComponent;
import net.dumbcode.projectnublar.entity.ik.components.IKTailComponent;
import net.dumbcode.projectnublar.entity.ik.parts.Segment;
import net.dumbcode.projectnublar.entity.ik.parts.WorldCollidingSegment;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.EntityLegWithFoot;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.StretchingIKChain;
import net.dumbcode.projectnublar.entity.ik.parts.sever_limbs.ServerLimb;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dinosaur extends PathfinderMob implements FossilRevived, GeoEntity, IKAnimatable<Dinosaur> {
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<String> idleAnimations = List.of("sniffingair", "sniffground", "speak1", "lookleft", "lookright", "scratching", "shakehead", "shakebody");
    public List<IKModelComponent<Dinosaur>> components = new ArrayList<>();

    public Dinosaur(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.setUpLimbs();
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        return new SmoothGroundNavigation(this, pLevel);
    }

    protected void setUpLimbs() {
        this.addComponent(new IKLegComponent<>(
                new IKLegComponent.LegSetting.Builder()
                        .maxDistance(1.5)
                        .standStillCounter(40)
                        .stepInFront(1)
                        .movementSpeed(0.4)
                        .steppingParabolaStrength(0.2)
                        .build(),
                List.of(new ServerLimb(0.7, 0, 0.1),
                        new ServerLimb(-0.7, 0, 0.1)),
                new EntityLegWithFoot(
                        new WorldCollidingSegment(
                                new Segment.Builder().length(0.5625).angleSize(40).angleOffset(70)
                        ),
                        new Segment.Builder().length(1.1).angleSize(50).build(),
                        new Segment.Builder().length(1.4).angleSize(40).angleOffset(60).build(),
                        new Segment.Builder().length(1.3).angleSize(30).angleOffset(-100).build()),
                new EntityLegWithFoot(
                        new WorldCollidingSegment(
                                new Segment.Builder().length(0.5625).angleSize(40).angleOffset(70)
                        ),
                        new Segment.Builder().length(1.1).angleSize(50).build(),
                        new Segment.Builder().length(1.4).angleSize(40).angleOffset(60).build(),
                        new Segment.Builder().length(1.3).angleSize(30).angleOffset(-100).build())
        ));

        this.addComponent(new IKTailComponent<>(
                new IKTailComponent.TailStretchingIKChain(
                        new WorldCollidingSegment(new Segment.Builder().length(1.4)),
                        new WorldCollidingSegment(new Segment.Builder().length(1.7)),
                        new WorldCollidingSegment(new Segment.Builder().length(1.3))
                )));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::predicate));
    }

    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }

    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }

    @Override
    public void push(Entity pEntity) {
        super.push(pEntity);
    }

    public ResourceLocation getTextureLocation() {
        if (true) { //What the fuck?
            return new ResourceLocation("projectnublar:textures/entity/tyrannosaurus_rex.png");
        }
        return this.entityData.get(DINO_DATA).getTextureLocation();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DINO_DATA, new DinoData());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", entityData.get(DINO_DATA).toNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag $$0) {
        super.readAdditionalSaveData($$0);
        entityData.set(DINO_DATA, DinoData.fromNBT($$0.getCompound("dino_data")));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomStrollGoal(this,1, 20));
    }

    protected PlayState predicate(AnimationState<GeoAnimatable> state) {
        AnimationController<?> controller = state.getController();
        if (isMoving()) {
            controller.setAnimation(RawAnimation.begin().thenLoop("walk"));
        } else {
            if (controller.hasAnimationFinished() || (controller.getCurrentAnimation() != null && Objects.equals(controller.getCurrentAnimation().animation().name(), "walk"))) {
                controller.setAnimation(RawAnimation.begin().thenLoop(idleAnimations.get(this.random.nextInt(idleAnimations.size()))));
            }
        }
        return PlayState.CONTINUE;
    }

    public boolean isMoving() {
        return this.xo != this.getX() || this.zo != this.getZ();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void tick() {
        super.tick();
        Player nearestPlayer = this.level().getNearestPlayer(this, 100);
        if (nearestPlayer != null && nearestPlayer.getMainHandItem().is(Items.BONE)) {
            this.navigation.moveTo(nearestPlayer, 1.0);
        }
        this.tickComponentsServer(this);
    }

    @Override
    public List<IKModelComponent<Dinosaur>> getComponents() {
        return this.components;
    }

    @Override
    public double getSize() {
        return (this.getDinoData().getGeneValue(Genes.SIZE) / 100) + 1.0;
    }
}
