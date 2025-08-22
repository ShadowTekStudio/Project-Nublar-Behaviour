package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.ai.FenceAwareNavigation;
import net.dumbcode.projectnublar.entity.ai.FenceAwareNodeEvaluator;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.api.IMultiPartDino;
import net.dumbcode.projectnublar.entity.behaviour.*;
import net.dumbcode.projectnublar.entity.behaviour.carnivore.RoarAtThreat;
import net.dumbcode.projectnublar.entity.behaviour.carnivore.SoloHuntRoamBehaviour;
import net.dumbcode.projectnublar.entity.behaviour.carnivore.SoloHuntingBehaviour;
import net.dumbcode.projectnublar.entity.sensors.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionEntity;
import net.dumbcode.projectnublar.entity.species.DinosaurPart;
import net.dumbcode.projectnublar.entity.species.PartEntity;
import net.dumbcode.projectnublar.entity.tasks.*;
import net.dumbcode.projectnublar.init.*;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyBlocksSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

import static net.dumbcode.projectnublar.util.DinoAnimationUtils.IS_ROARING_STATE;

public abstract class Dinosaur extends TamableAnimal implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur>,GeoAnimatable{
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.COMPOUND_TAG);
    public static EntityDataAccessor<Optional<UUID>> DINO_FAMILY_UUID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Optional<UUID>> DINO_MATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Boolean> BABY_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> JUVENILE_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> SUB_ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> ADULT_DATA_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DINOSAUR_PACK_ID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Vector3f> DINOSAUR_HEAD_POS = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.VECTOR3);
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected @Nullable DinoBehaviourData cachedBehaviourData;

    private DinoDietData dietData;

    public  DinosaurPart head;

    public @Nullable Vec3 headBonePos;

    private int hungerDrainTick;
    private int staminaDrainTick;
    private int thirstDrainTick;
    private int socialDrainTick;
    private int breedingCoolDown = 500;
    private int flinchAnimLength;

    public DinosaurPart[] subEntities;

    public Dinosaur(EntityType<? extends Dinosaur> $$0, Level $$1, int flinchAnimLength) {
        super($$0, $$1);
        this.flinchAnimLength = flinchAnimLength;

    }
    //ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkController(this));
        controllers.add(new AnimationController<GeoAnimatable>(this, "dino_controller",0,this::animationPredicate));
        controllers.add(new AnimationController<GeoAnimatable>(this, "dino_secondary_Controller",0,this::animationPredicateAmbient));
    }
    private <T extends GeoAnimatable> PlayState animationPredicateAmbient(AnimationState<T> state) {

    return PlayState.STOP;

    }

    //STOP THE GAME DESPAWNING AFTER DEATH
    @Override
    protected void tickDeath() {
    }

    public boolean isRunning(){
        return this.entityData.get(DinoAnimationUtils.IS_RUNNING_STATE);
    }
    public boolean isFlinching(){
        return this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE);
    }

    private <T extends GeoAnimatable> PlayState animationPredicate(AnimationState<T> state) {
        if(this.isDeadOrDying()){
            return state.setAndContinue(DinoAnimationUtils.DEAD_ANIM);
        }
        if(this.isFlinching()){
            return state.setAndContinue(DinoAnimationUtils.FLINCH_ANIM);
        }

        /// TO-DO: Fix running animation
        /*
        if(this.isRunning() && !this.isSwimming() && state.isMoving()){
            return state.setAndContinue(DinoAnimationUtils.RUN_ANIM);
        }
        if(this.isRunning() && !state.isMoving()){
            DinoAnimationUtils.setAnimationState(this,"run",false);
        }
         */
        if(this.isRoaring()){
            return state.setAndContinue(DinoAnimationUtils.ROARING_ANIM);
        }

        if(this.isAttacking()){
            return state.setAndContinue(DinoAnimationUtils.ATTACK_ANIM);
        }
        if(this.isSitting()){
            return state.setAndContinue(DinoAnimationUtils.REST_ANIM);
        }
        if(this.isRising()){
            return state.setAndContinue(DinoAnimationUtils.GETTING_UP_ANIM);
        }
        if(this.isResting()) {
            return state.setAndContinue(DinoAnimationUtils.REST_IDLE_ANIM);
        }
        if(this.isDrinking()) {
            return state.setAndContinue(DinoAnimationUtils.DRINKING_ANIM);
        }
        if (this.isEating()) {
            return  state.setAndContinue(DinoAnimationUtils.EATING_ANIM);
        }
        if (this.isInWater() && state.isMoving()){
            return state.setAndContinue(DinoAnimationUtils.SWIM_ANIM);
        }
        if(!state.isMoving() && this.isIdle()){
            return state.setAndContinue(DinoAnimationUtils.IDLE_ANIM);
        }

        return PlayState.STOP;
    }

    //MAIN DATA GETTERS
    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }

    public DinoBehaviourData getDinoBehaviour(){
        if(this.cachedBehaviourData == null){
            this.cachedBehaviourData = DinoBehaviourData.fromNBT(this.entityData.get(DINO_BEHAVIOUR));
        }
        return this.cachedBehaviourData;
    }

    public DinoDietData getDinoDiet(){
        if(this.dietData == null){
            this.dietData = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        }
        return this.dietData;
    }
    public void spawnInteractionEntity(){

    }
    public void setInteractionEntity(TyrannosaurInteractionEntity pInteractionEntity){

    }
    public @Nullable TyrannosaurInteractionEntity getInteractionEntity() {
        return null;
    }
    public void clearInteractionEntity(){

    }
    //DATA SYNC
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DINO_DATA, new DinoData());
        this.entityData.define(DINO_BEHAVIOUR, new CompoundTag());
        this.entityData.define(DINO_FAMILY_UUID, Optional.empty());
        this.entityData.define(DINO_MATE, Optional.empty());
        this.entityData.define(DinoNeedsUtils.HUNGER, 100.0F);
        this.entityData.define(DinoNeedsUtils.THIRST, 100.0F);
        this.entityData.define(DinoNeedsUtils.STAMINA, 100.0F);
        this.entityData.define(DinoNeedsUtils.SOCIAL, 100.0F);
        this.entityData.define(DinoNeedsUtils.AGGRESSION, 100.0F);
        this.entityData.define(DinoNeedsUtils.DOMESTICITY, 100.0F);
        this.entityData.define(DinoNeedsUtils.FERTILITY, 100.0F);
        this.entityData.define(DinoNeedsUtils.IMMUNITY, 100.0F);
        this.entityData.define(DinoNeedsUtils.INTELLIGENCE, 100.0F);
        this.entityData.define(DinoNeedsUtils.SIZE, 1F);
        this.entityData.define(DinoNeedsUtils.TAMING_SCORE, 100.0F);
        this.entityData.define(DinoNeedsUtils.VISION, 100.0F);
        this.entityData.define(DINOSAUR_HEAD_POS, new Vector3f());
        this.entityData.define(BABY_DATA_ID, false);
        this.entityData.define(JUVENILE_DATA_ID, false);
        this.entityData.define(SUB_ADULT_DATA_ID, false);
        this.entityData.define(ADULT_DATA_ID, true);
        this.entityData.define(DinoAnimationUtils.IS_EATING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_DRINKING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_NESTING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_RESTING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_RISING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SITTING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_ATTACKING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_FLINCHING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_DEAD_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SWIMMING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_RUNNING_STATE, false);
        this.entityData.define(DinoAnimationUtils.LOOKING_LEFT_STATE, false);
        this.entityData.define(DinoAnimationUtils.LOOKING_RIGHT_STATE, false);
        this.entityData.define(DinoAnimationUtils.TURNING_RIGHT_STATE, false);
        this.entityData.define(DinoAnimationUtils.TURNING_LEFT_STATE, false);
        this.entityData.define(IS_ROARING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SPEAKING_STATE, false);
        this.entityData.define(DINOSAUR_PACK_ID, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", this.getDinoData().toNBT());
        tag.put("behaviour_profile", this.entityData.get(DINO_BEHAVIOUR));
        tag.putFloat("hunger_bar", this.entityData.get(DinoNeedsUtils.HUNGER));
        tag.putFloat("thirst_bar", this.entityData.get(DinoNeedsUtils.THIRST));
        tag.putFloat("stamina_bar", this.entityData.get(DinoNeedsUtils.STAMINA));
        tag.putFloat("social_bar", this.entityData.get(DinoNeedsUtils.SOCIAL));
        tag.putFloat("trust_threshold", this.entityData.get(DinoNeedsUtils.TAMING_SCORE));
        tag.putFloat("dino_vision", this.entityData.get(DinoNeedsUtils.VISION));
        tag.putFloat("dino_aggression", this.entityData.get(DinoNeedsUtils.AGGRESSION));
        tag.putFloat("dino_fertility", this.entityData.get(DinoNeedsUtils.FERTILITY));
        tag.putFloat("dino_domesticity", this.entityData.get(DinoNeedsUtils.DOMESTICITY));
        tag.putFloat("dino_size", this.entityData.get(DinoNeedsUtils.SIZE));
        tag.putFloat("dino_intelligence", this.entityData.get(DinoNeedsUtils.INTELLIGENCE));
        tag.putFloat("dino_immunity", this.entityData.get(DinoNeedsUtils.IMMUNITY));
        tag.putBoolean("baby_age_boolean", this.entityData.get(BABY_DATA_ID));
        tag.putBoolean("juvenile_age_boolean", this.entityData.get(JUVENILE_DATA_ID));
        tag.putBoolean("sub_adult_age_boolean", this.entityData.get(SUB_ADULT_DATA_ID));
        tag.putBoolean("adult_age_boolean", this.entityData.get(ADULT_DATA_ID));
        tag.putInt("dino_pack_id", this.entityData.get(DINOSAUR_PACK_ID));

        if(headBonePos != null){
            tag.putDouble("headx",headBonePos.x);
            tag.putDouble("heady",headBonePos.y);
            tag.putDouble("headz",headBonePos.z);
        }

        if(this.entityData.get(DINO_MATE).isPresent()) {
            tag.putUUID("mate_uuid",this.entityData.get(DINO_MATE).get());
        }
        if(this.entityData.get(DINO_FAMILY_UUID).isPresent()) {
            tag.putUUID("family_uuid",this.entityData.get(DINO_FAMILY_UUID).get());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pTag) {
        super.readAdditionalSaveData(pTag);
        entityData.set(DINO_DATA, DinoData.fromNBT(pTag.getCompound("dino_data")));
        this.entityData.set(DINO_BEHAVIOUR, pTag.getCompound("behaviour_profile"));
        this.entityData.set(DinoNeedsUtils.HUNGER, pTag.getFloat("hunger_bar"));
        this.entityData.set(DinoNeedsUtils.THIRST, pTag.getFloat("thirst_bar"));
        this.entityData.set(DinoNeedsUtils.STAMINA, pTag.getFloat("stamina_bar"));
        this.entityData.set(DinoNeedsUtils.SOCIAL, pTag.getFloat("social_bar"));
        this.entityData.set(BABY_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(JUVENILE_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(SUB_ADULT_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(ADULT_DATA_ID, pTag.getBoolean("baby_age_boolean"));
        this.entityData.set(DINOSAUR_PACK_ID, pTag.getInt("dinosaur_pack_id"));
        this.entityData.set(DinoNeedsUtils.TAMING_SCORE,pTag.getFloat("trust_threshold"));
        this.entityData.set(DinoNeedsUtils.VISION,pTag.getFloat("dino_vision"));
        this.entityData.set(DinoNeedsUtils.AGGRESSION,pTag.getFloat("dino_aggression"));
        this.entityData.set(DinoNeedsUtils.FERTILITY,pTag.getFloat("dino_fertility"));
        this.entityData.set(DinoNeedsUtils.DOMESTICITY,pTag.getFloat("dino_domesticity"));
        this.entityData.set(DinoNeedsUtils.SIZE,pTag.getFloat("dino_size"));
        this.entityData.set(DinoNeedsUtils.INTELLIGENCE,pTag.getFloat("dino_intelligence"));
        this.entityData.set(DinoNeedsUtils.IMMUNITY,pTag.getFloat("dino_immunity"));

        if(pTag.contains("headx") && pTag.contains("heady") && pTag.contains("headz")){
           headBonePos = new Vec3(pTag.getDouble("headx"),pTag.getDouble("heady"),pTag.getDouble("headz"));
        }

        if(pTag.contains("mate_uuid")){
            Optional<UUID> mate_uuid = Optional.of(pTag.getUUID("mate_uuid"));
            this.entityData.set(DINO_MATE, mate_uuid);
        } else this.entityData.set(DINO_MATE, Optional.empty());

        if(pTag.contains("family_uuid")){
            Optional<UUID> mate_uuid = Optional.of(pTag.getUUID("family_uuid"));
            this.entityData.set(DINO_FAMILY_UUID, mate_uuid);
        } else this.entityData.set(DINO_FAMILY_UUID, Optional.empty());

    }

    public void setHeadPositon(Vec3 worldPos){
      Vector3f pos = worldPos.toVector3f();
      this.entityData.set(DINOSAUR_HEAD_POS, pos);
    }
    public @Nullable Vec3 getHeadBonePos(){
        return new Vec3(this.entityData.get(DINOSAUR_HEAD_POS));
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    //MAIN DATA SETTERS
    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }

    public void setDinoBehaviour(CompoundTag behaviourData){
        this.entityData.set(DINO_BEHAVIOUR, behaviourData);
    }

    //TARGETING BOOLEANS FOR BRAIN
    public boolean canTargetWaterSource(BlockState entity){
        if(this.isSleeping()){
            return false;
        }

           return entity.is(Blocks.WATER);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        if(this.isSleeping()){
            return false;
        }
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getDescriptionId());
    }
    public boolean hurtFromPart(DinosaurPart part, DamageSource source, float amount) {
        return this.hurt(this.damageSources().generic(), amount);
    }

    public boolean canTarget(LivingEntity target) {
       return target.getVehicle() != this;
    }

    public boolean canRival(LivingEntity target) {

        if(target instanceof Dinosaur dinosaur && this.getDinoData().getBaseDino() == dinosaur.getDinoData().getBaseDino()){
                return target.getVehicle() != this && !this.isFamily(dinosaur);
        } else {
            return false;
        }
    }

    public boolean isChild(){
        return this.isBaby() || this.isJuvanile();
    }

    public boolean isHuntingBlocked(){
        return BrainUtils.hasMemory(this,MemoryTypesInit.IS_RESTING.get()) ||BrainUtils.hasMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM) || BrainUtils.hasMemory(this, MemoryTypesInit.IS_EATING.get()) ||
                BrainUtils.hasMemory(this, MemoryTypesInit.IS_DRINKING.get());
    }

    public boolean canTargetFoodItem(ItemEntity target) {
        if(this.isSleeping()){
            return false;
        }
        return this.getDinoDiet().foodMap().containsKey(target.getItem().getDescriptionId());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getDescriptionId());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FenceAwareNavigation(this,level);
    }


    @Override
    public int getMaxHeadYRot() {
        return 2;
    }

    @Override
    public int getHeadRotSpeed() {
        return 2;
    }

    public boolean hasWantedLookTarget(){
        double x;
        double z;

        if(this.getLookControl() != null) {
            x = this.getLookControl().getWantedX();
            z = this.getLookControl().getWantedZ();
        } else{
            x = 0.0;
            z = 0.0;
        }
    return x != 0.0 && z != 0.0;
    }


    @Override
    public void setYRot(float yRot) {
        float currentYaw = super.getYRot();
        float delta = Mth.wrapDegrees(yRot - currentYaw);

        float maxTurn = 10.0F;

            if (delta > maxTurn) {
                delta = maxTurn;
                super.setYRot(currentYaw + delta);
            } else if (delta < -maxTurn) {
                delta = -maxTurn;
                super.setYRot(currentYaw + delta);
            } else {
                super.setYRot(yRot);
            }
    }
    //BRAIN

    @Override
    protected Brain.Provider<Dinosaur> brainProvider() {
        return new SmartBrainProvider<>(this);
    }



    @Override
    public double getMeleeAttackRangeSqr(LivingEntity entity) {
        return (double)(this.getBbWidth() * 3.0F * this.getBbWidth() * 3.0F + entity.getBbWidth());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((block, dinosaur) -> dinosaur.canTargetWaterSource(block)
                && !BrainUtils.hasMemory(dinosaur,MemoryTypesInit.BRAIN_OVERRIDE.get()));
        waterSourceSensor.setRadius(20);

        NearbyBlocksSensor<Dinosaur> fenceProximinitySensor = new NearbyBlocksSensor<>();
        fenceProximinitySensor.setRadius(2.0);
        fenceProximinitySensor.setPredicate((block, dinosaur) -> block.is(BlockInit.ELECTRIC_FENCE.get()) && DinoNeedsUtils.starving(dinosaur));

        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((item, dinosaur) -> dinosaur.canTargetFoodItem(item)
                && !BrainUtils.hasMemory(dinosaur,MemoryTypesInit.BRAIN_OVERRIDE.get())) ;
        foodItemSensor.setRadius(20);

        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
     //  nearbyLivingEntitySensor.setPredicate((target, dinosaur) -> !BrainUtils.hasMemory(dinosaur,MemoryTypesInit.BRAIN_OVERRIDE.get()));


        return List.of(
                waterSourceSensor,
                nearbyLivingEntitySensor,
                foodItemSensor,
                fenceProximinitySensor
        );
    }


    @Override
    public BrainActivityGroup<? extends Dinosaur> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new DinosaurLookAtTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
                new MoveToWalkTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
               // new SetMateFromNearbyDinosaurs<>(), needs work dead dinosaurs seem to still be able to breed
               new Panic<>(),
                new SetHunting<>(),
                new RoarAtThreat<>(34) //- needs to be made more situational so it happens more
                .whenStarting(dinosaur -> dinosaur.entityData.set(IS_ROARING_STATE, true))
                .whenStopping(dinosaur -> dinosaur.entityData.set(IS_ROARING_STATE,false)),
               new DrinkBehaviour<>(100)
                       .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"drink",true))
                       .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"drink",false)),
                new EatBehaviour<>(69)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"eat",true))
                        .whenStopping(dinosaur ->  DinoAnimationUtils.setAnimationState(dinosaur,"eat",false)),
                new RestingBehaviour<>(69)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"sit",true))
                        .whenStopping(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"rest",false)),
                new GettingUpFromRestBehaviour<>(69)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState(dinosaur,"eat",true))
                        .whenStopping(dinosaur ->DinoAnimationUtils.setAnimationState(dinosaur,"getup",false))
        );
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getIdleTasks() {
        // These are the tasks that run when the mob isn't doing anything else (usually)
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                        new SetWalkTargetToWaterSource<>()
                                .closeEnoughWhen((entity, pos)-> 3),
                        new SetWalkTargetToFoodItem<>().predicate((dinosaur, item) -> dinosaur.canTargetFoodItem(item)),
                        new FollowParent<>().parentPredicate((baby, parent)-> baby instanceof Dinosaur dino && dino.isFamily(parent) && !parent.isBaby()),
                        new StartTurfWar<>().attackablePredicate((entity -> canRival(entity))),
                        new SoloHuntingBehaviour<>()
                               .attackablePredicate(entity -> canTarget(entity))
                               .startCondition(dinosaur -> dinosaur instanceof CarnivoreDinosaur),
                        new SoloHuntRoamBehaviour<>()
                                .dontAvoidWater()
                                .setRadius(40.0D)
                                .stopIf(dino -> BrainUtils.hasMemory(dino, MemoryModuleType.ATTACK_TARGET))),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<>().dontAvoidWater().setRadius(10.0, 4.0).walkTargetPredicate((dinosaur, pos)-> !BrainUtils.hasMemory(dinosaur,MemoryTypesInit.BRAIN_OVERRIDE.get()))
                )
        );
    }
    @Override
    public BrainActivityGroup<? extends Dinosaur> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> (target instanceof Player pl && (pl.isCreative() || pl.isSpectator())) || target.isDeadOrDying()),
                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"run",true)),
                new BreakFenceBehaviour<>(20),
                new AnimatableMeleeAttack<>(20)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",true))
                        .whenStopping(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",false))
        );
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1,new BreedGoal(this, 1.0));
    }

    private int flinchAnimTicks;

    @Override
    public void tick() {
        super.tick();

        if(this.entityData.get(DinoAnimationUtils.IS_FLINCHING_STATE)){
            flinchAnimTicks++;
            if(flinchAnimTicks > this.flinchAnimLength){
                this.entityData.set(DinoAnimationUtils.IS_FLINCHING_STATE, false);
                flinchAnimTicks = 0;
            }
        }

        if(!level().isClientSide()) {
            this.hungerDrainTick++;
            this.thirstDrainTick++;
         //   this.socialDrainTick++;
            this.staminaDrainTick++;

            if(this.isBaby() && !this.entityData.get(BABY_DATA_ID)){
                this.entityData.set(BABY_DATA_ID, true);
            } else if (!this.isBaby() && this.entityData.get(BABY_DATA_ID)){
                this.entityData.set(BABY_DATA_ID, false);
            }
            if(this.isJuvanile() && !this.entityData.get(JUVENILE_DATA_ID)){
                this.entityData.set(JUVENILE_DATA_ID, true);
            } else if (!this.isJuvanile() && this.entityData.get(JUVENILE_DATA_ID)){
                this.entityData.set(JUVENILE_DATA_ID, false);
            }
            if(this.isSubAdult() && !this.entityData.get(SUB_ADULT_DATA_ID)){
                this.entityData.set(SUB_ADULT_DATA_ID, true);
            } else if (!this.isSubAdult() && this.entityData.get(SUB_ADULT_DATA_ID)){
                this.entityData.set(SUB_ADULT_DATA_ID, false);
            }
            if(this.age >= 0 && !this.entityData.get(ADULT_DATA_ID)){
                this.entityData.set(ADULT_DATA_ID, true);
            } else if (this.age < 0 && this.entityData.get(ADULT_DATA_ID)){
                this.entityData.set(ADULT_DATA_ID, false);
            }

          if(this.breedingCoolDown == 0){
                if(this.getDinoGender() == 1.0F && this.hasMate()) {
                    this.tryBreedWithMate();
                    this.breedingCoolDown++;
                }
            }
            if(this.breedingCoolDown >= 1){
                this.breedingCoolDown++;
            }
            if(this.breedingCoolDown > 2000){
                this.breedingCoolDown = 0;
            }

            if(this.hungerDrainTick >= this.getDinoBehaviour().hungerTickRate()){
                DinoNeedsUtils.tickHunger(this);
                if(DinoNeedsUtils.isHungry(this) && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_HUNGRY.get())){
                    BrainUtils.setMemory(this, MemoryTypesInit.IS_HUNGRY.get(), true);
                }
                this.hungerDrainTick = 0;
            }
            if(this.thirstDrainTick >= this.getDinoBehaviour().thirstTickRate()){
                DinoNeedsUtils.tickThirst(this);
                if(DinoNeedsUtils.isThirsty(this) && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_THIRSTY.get())){
                    BrainUtils.setMemory(this, MemoryTypesInit.IS_THIRSTY.get(), true);
                }
                this.thirstDrainTick = 0;
            }
            if(this.staminaDrainTick >= this.getDinoBehaviour().energyTickRate()){
                DinoNeedsUtils.tickStamina(this);
                if(DinoNeedsUtils.isTired(this) && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_TIRED.get())){
                    BrainUtils.setMemory(this, MemoryTypesInit.IS_TIRED.get(), true);
                }
                this.staminaDrainTick = 0;
            }
            if(this.socialDrainTick >= 300){
                DinoNeedsUtils.tickSocial();
                this.socialDrainTick = 0;
            }
            if(!DinoNeedsUtils.isHungry(this) && BrainUtils.hasMemory(this,MemoryTypesInit.HUNTING.get())){
                BrainUtils.clearMemory(this, MemoryTypesInit.HUNTING.get());
            }
        }
    }
    Random random = new Random();

    public void tryBreedWithMate(){
        int attempt = random.nextInt(0,100);
        if(attempt > 50) {
            this.setInLove(null);
            @Nullable Dinosaur mate = BrainUtils.getMemory(this, MemoryTypesInit.MATE.get());

            if(mate != null) {
                mate.setInLove(null);
            } else {
                this.clearDinoMate();
            }
        }
    }

    //ATTRIBUTES
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35)
                .add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .add(AttributesInit.DINO_ENERGY_NEED.get(),100)
                .add(AttributesInit.DINO_THIRST_NEED.get(), 100)
                .add(AttributesInit.DINO_HUNGER_NEED.get(),100)
                .add(AttributesInit.DINO_SOCIAL_NEED.get(),100)
                .add(AttributesInit.TRUST_SCORE.get(),1000)
                .add(AttributesInit.DINO_VISION.get(),100)
                .add(AttributesInit.DINO_AGGRESSION.get(), 0)
                .add(AttributesInit.DINO_INTELLIGENCE.get(), 50)
                .add(AttributesInit.DINO_FERTILITY.get(),50)
                .add(AttributesInit.DINO_IMMUNITY.get(),0);
    }

    public boolean isDrinking() {
        return this.entityData.get(DinoAnimationUtils.IS_DRINKING_STATE);
    }
    public boolean isEating() {return this.entityData.get(DinoAnimationUtils.IS_EATING_STATE);}
    public boolean isSitting() {return this.entityData.get(DinoAnimationUtils.IS_SITTING_STATE);}
    public boolean isRising() {
        return this.entityData.get(DinoAnimationUtils.IS_RISING_STATE);
    }
    public boolean isResting() {
        return this.entityData.get(DinoAnimationUtils.IS_RESTING_STATE);
    }
    public boolean isRoaring() {
        return this.entityData.get(IS_ROARING_STATE);
    }
    public boolean isAttacking() {return this.entityData.get(DinoAnimationUtils.IS_ATTACKING_STATE);}
    public boolean isIdle(){return !this.isDrinking() && !this.isEating() && !this.isResting() && !this.isRoaring() && !this.isAttacking();}

    public double getDinoGender() {
        //Gets Gender and if none has been set then returns as female.
        double gender = this.getDinoData().getGeneValue(GeneInit.GENDER.get());
        if (gender != 0){
            return gender;
        } else return 1;
    }
    @Nullable
    public Map<Player,Integer> getPlayerReputationMap(){
        if(BrainUtils.hasMemory(this, MemoryTypesInit.PLAYER_REPUTATION.get())){
            return BrainUtils.getMemory(this, MemoryTypesInit.PLAYER_REPUTATION.get());
        } else return null;
    }

    public int getReputationForPlayer(Player player){
        if(this.getPlayerReputationMap() != null){
            return this.getPlayerReputationMap().getOrDefault(player, 0);
        } else return 0;
    }
    public void increaseReputationForPlayer(Player player, int increase){
        if(this.getPlayerReputationMap() == null || !this.getPlayerReputationMap().containsKey(player)){
            return;
        }
        Map<Player,Integer> reputationMap = this.getPlayerReputationMap();
        int currentReputation = reputationMap.get(player);
        reputationMap.remove(player);
        reputationMap.put(player,currentReputation + increase);
    }
    public void decreaseReputationForPlayer(Player player, int decrease){
        if(this.getPlayerReputationMap() == null || !this.getPlayerReputationMap().containsKey(player)){
            return;
        }
        Map<Player,Integer> reputationMap = this.getPlayerReputationMap();
        int currentReputation = reputationMap.get(player);
        reputationMap.remove(player);
        reputationMap.put(player,currentReputation - decrease);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        EntityType<?> babyType = this.getDinoData().getBaseDino();
        return (Dinosaur) babyType.create(serverLevel);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        @Nullable Dinosaur dinosaur = (Dinosaur) this.getBreedOffspring(level, mate);
        Dinosaur mother;
        if(this.getDinoGender() == 1){
            mother = this;
        } else mother = (Dinosaur) mate;

        if(mate instanceof Dinosaur) {
            if (dinosaur != null) {
                dinosaur.setDinoData(mother.getDinoData());
                DinoNeedsUtils.setDinoBaseNeeds(dinosaur, mother.getDinoBehaviour());
                dinosaur.setDinoBehaviour(mother.getDinoBehaviour().toNBT(mother.getDinoBehaviour()));
                dinosaur.getDinoData().setGeneValue(GeneInit.GENDER.get(), random.nextInt(1,2));
                dinosaur.setBaby(true);
                dinosaur.setDinoFamilyUuid(this.getFamilyId());
                dinosaur.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                this.finalizeSpawnChildFromBreeding(level, mate, dinosaur);
                level.addFreshEntityWithPassengers(dinosaur);
            }
        }
    }

    @Override
    public boolean isBaby() {
        int age = this.age;
        return age <= -18000;
    }

    public boolean isJuvanile(){
        int age = this.age;
        return age <= -12000 && age > -18000;
    }

    public boolean isSubAdult(){
        int age = this.age;
        return age <= -6000 && age > -12000;
    }

    public int getGrowthStage(){
        if(this.entityData.get(BABY_DATA_ID)){
            return 1;
        }
        else if(this.entityData.get(JUVENILE_DATA_ID)){
            return 2;
        }
        else if(this.entityData.get(SUB_ADULT_DATA_ID)){
            return 3;

        } else return 4;
    }


    public void createDinosaurFamily(Dinosaur mate){
        UUID newFamilyId = UUID.randomUUID();
        this.setDinoFamilyUuid(newFamilyId);
        mate.setDinoFamilyUuid(newFamilyId);
    }
    public void setDinoFamilyUuid(UUID familyUuid){
        this.entityData.set(DINO_FAMILY_UUID,Optional.of(familyUuid));
    }
    public @Nullable UUID getFamilyId(){
        if(this.entityData.get(DINO_FAMILY_UUID).isPresent()) {
            return this.entityData.get(DINO_FAMILY_UUID).get();
        } else return null;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return this.isMate(otherAnimal.getUUID());
    }

    @Override
    public boolean canBreed() {
        return !this.isBaby() && !this.isJuvanile() && !this.isSubAdult();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.STICK) && !(this.getLastHurtByMob() != null && this.getLastAttacker().is(player));
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.heal((float) item.getFoodProperties().getNutrition());
                return InteractionResult.SUCCESS;
            } else {
                InteractionResult interactionresult = super.mobInteract(player, hand);
                if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    return InteractionResult.SUCCESS;
                } else {
                    return interactionresult;
                }
            }
        } else if (itemStack.is(Items.STICK)) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            if (this.random.nextInt(3) == 0) {
                this.increaseReputationForPlayer(player,10);

                if(!this.isTame() && this.getReputationForPlayer(player) > 100){
                    this.tame(player);
                }
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;


        } else {
            return super.mobInteract(player, hand);
        }
    }

    public boolean canMateWith(Dinosaur pDinosaur, Dinosaur pMate){

        if(pDinosaur.isBaby() || pMate.isBaby()){
            return false;
        }if(pDinosaur.isJuvanile() || pMate.isJuvanile()){
            return false;
        }if(pDinosaur.isSubAdult() || pMate.isSubAdult()) {
            return false;
        }
        if(!pDinosaur.isAlive() || !pMate.isAlive()){
            return false;
        }
        if(pDinosaur.hasMate() || pMate.hasMate()){
            return false;
        }

        return pDinosaur.getDinoGender() != pMate.getDinoGender();
    }

    public boolean isMate(UUID mateId){
        if(this.entityData.get(DINO_MATE).isPresent()) {
            return mateId == this.entityData.get(DINO_MATE).get();
        } else return false;
    }
    public boolean isFamily(LivingEntity pMob){
        if(pMob instanceof Dinosaur mob){
            if(mob.getFamilyId() != null && this.getFamilyId() != null){
            return mob.getFamilyId().equals(this.getFamilyId());
            } else return false;
        } else return false;
    }

    public boolean hasMate(){
        return !this.entityData.get(DINO_MATE).isEmpty();
    }
    public void registerDinoMate(UUID pMateId){
        this.entityData.set(DINO_MATE,Optional.of(pMateId));
    }
    public void clearDinoMate(){
        this.entityData.set(DINO_MATE, Optional.empty());
    }

    //SKIN SETTER
    public Color layerColor(int layer, DinoLayer dinoLayer) {
        if (dinoLayer != null && dinoLayer.getBasicLayer() == -1) {
            return Color.WHITE;
        }
        if (layer >= this.getDinoData().getLayerColors().stream().count()) {
            return new Color(Mth.floor(this.getDinoData().getLayerColor(dinoLayer.getBasicLayer())));
        }
        return new Color(Mth.floor(this.getDinoData().getLayerColor(layer)));
    }

    public @Nullable SoundEvent getRoarSound(){
        return null;
    }
    public @Nullable SoundEvent getAttackGrowlSound(){
        return null;
    }
    public @Nullable SoundEvent getAttackSound(){
        return null;
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        if(damageSource.getEntity() instanceof Player player){
            this.decreaseReputationForPlayer(player, 20);
            if(this.isTame() && this.getOwner().is(player) && this.getReputationForPlayer(player) < 40){
                this.setTame(false);
                this.setOwnerUUID(null);
            }
        }
        DinoAnimationUtils.setAnimationState(this,"flinch", true);
        super.actuallyHurt(damageSource, damageAmount);

    }
}
