package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.behaviour.IdleAnimationBehaviour;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Objects;

public class Dinosaur extends PathfinderMob implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur> {
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Dinosaur(EntityType<? extends PathfinderMob> $$0, Level $$1) {
        super($$0, $$1);
    }

    public static final String MAIN_CONTROLLER = "controller";

    public static List<String> idleAnimations = List.of("sniffingair", "sniffground", "speak1", "lookleft", "lookright", "scratching", "shakehead", "shakebody");


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<Dinosaur> controller = new AnimationController<>(this, MAIN_CONTROLLER, 5, event -> {
            AnimationController<Dinosaur> controller = state.getController();
            if (state.isMoving()) {
                return state.setAndContinue(DefaultAnimations.WALK);
            } else {
                return state.setAndContinue(DefaultAnimations.IDLE);
            }
        });

        for (String string : idleAnimations) {
            controller.triggerableAnim(string,RawAnimation.begin().thenPlay(string));
        }

        controllers.add(controller);
    }

    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }

    @Override
    public void checkDespawn() {

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35).add(Attributes.MOVEMENT_SPEED, .5)
                .add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.ARMOR, 2).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    public Color layerColor(int layer, DinoLayer dinoLayer) {
        if (dinoLayer != null && dinoLayer.getBasicLayer() == -1) {
            return Color.WHITE;
        }
        if (layer >= this.getDinoData().getLayerColors().stream().count()) {
            return new Color(Mth.floor(this.getDinoData().getLayerColor(dinoLayer.getBasicLayer())));
        }
        return new Color(Mth.floor(this.getDinoData().getLayerColor(layer)));
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

    public boolean isMoving() {
        return this.xo != this.getX() || this.zo != this.getZ();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
        nearbyLivingEntitySensor.setPredicate((target, entity) -> canTarget(target));
        return List.of(nearbyLivingEntitySensor, // This tracks nearby entities
                new HurtBySensor<>());
    }

    public boolean canTarget(LivingEntity target) {
        if (target instanceof Dinosaur) return false;
        if (target instanceof Player player && player.isCreative()) return false;
        if (target.isDeadOrDying()) return false;
        return target.getVehicle() != this;
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),                      // Have the entity turn to face and look at its current look target
                new MoveToWalkTarget<>());
    }

    @Override
    protected Brain.Provider<Dinosaur> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickBrain(this);
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getIdleTasks() {
        // These are the tasks that run when the mob isn't doing anything else (usually)
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(      // Run only one of the below behaviours, trying each one in order.
                        new TargetOrRetaliate<>()
                                .attackablePredicate(entity -> canTarget(entity)),            // Set the attack target and walk target based on nearby entities
                        // Set the attack target and walk target based on nearby entities
                        new SetPlayerLookTarget<>(),          // Set the look target for the nearest player
                        new SetRandomLookTarget<>()),         // Set a random look target
                new OneRandomBehaviour<>(                 // Run a random task from the below options
                        new SetRandomWalkTarget<>().setRadius(16, 8),          // Set a random walk target to a nearby position
                        new IdleAnimationBehaviour<>().runFor(entity -> entity.getRandom().nextInt(50, 100)))); // Do nothing for 1.5->3 seconds
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> target instanceof Player pl && (pl.isCreative() || pl.isSpectator())), // Cancel fighting if the target is no longer valid
                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f),      // Set the walk target to the attack target
                
                new AnimatableMeleeAttack<>(4)
        ); // Melee attack the target if close enough
    }

}
