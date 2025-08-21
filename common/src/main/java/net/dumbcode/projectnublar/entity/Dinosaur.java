package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.behaviour.DrinkBehaviour;
import net.dumbcode.projectnublar.entity.behaviour.EatBehaviour;
import net.dumbcode.projectnublar.entity.behaviour.GettingUpFromRestBehaviour;
import net.dumbcode.projectnublar.entity.behaviour.RestingBehaviour;
import net.dumbcode.projectnublar.entity.sensors.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.entity.tasks.SetMateFromNearbyDinosaurs;
import net.dumbcode.projectnublar.entity.tasks.SetWalkTargetToFoodItem;
import net.dumbcode.projectnublar.entity.tasks.SetWalkTargetToWaterSource;
import net.dumbcode.projectnublar.init.*;
import net.dumbcode.projectnublar.util.DinoAnimationUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public abstract class Dinosaur extends TamableAnimal implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur> {
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.COMPOUND_TAG);
    public static EntityDataAccessor<Optional<UUID>> DINO_FAMILY_UUID = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Optional<UUID>> DINO_MATE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.OPTIONAL_UUID);
    public static EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Float> THIRST = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Float> SOCIAL = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);

    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected @Nullable DinoBehaviourData cachedBehaviourData;

    private DinoDietData dietData;

    private int hungerDrainTick;
    private int staminaDrainTick;
    private int thirstDrainTick;
    private int socialDrainTick;
    private int breedingCoolDown = 500;

    public Dinosaur(EntityType<? extends TamableAnimal> $$0, Level $$1) {
        super($$0, $$1);

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
    protected void tickDeath() {}

    private <T extends GeoAnimatable> PlayState animationPredicate(AnimationState<T> state) {
        if(this.isDeadOrDying()){
            return state.setAndContinue(DinoAnimationUtils.DEAD_ANIM);
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

    //DATA SYNC
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DINO_DATA, new DinoData());
        this.entityData.define(DINO_BEHAVIOUR, new CompoundTag());
        this.entityData.define(DINO_FAMILY_UUID, Optional.empty());
        this.entityData.define(DINO_MATE, Optional.empty());
        this.entityData.define(HUNGER, 100.0F);
        this.entityData.define(THIRST, 100.0F);
        this.entityData.define(STAMINA, 100.0F);
        this.entityData.define(SOCIAL, 100.0F);
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
        this.entityData.define(DinoAnimationUtils.IS_ROARING_STATE, false);
        this.entityData.define(DinoAnimationUtils.IS_SPEAKING_STATE, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", this.getDinoData().toNBT());
        tag.put("behaviour_profile", this.entityData.get(DINO_BEHAVIOUR));
        tag.putFloat("hunger_bar", this.entityData.get(HUNGER));
        tag.putFloat("thirst_bar", this.entityData.get(THIRST));
        tag.putFloat("stamina_bar", this.entityData.get(STAMINA));
        tag.putFloat("social_bar", this.entityData.get(SOCIAL));

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
        this.entityData.set(HUNGER, pTag.getFloat("hunger_bar"));
        this.entityData.set(THIRST, pTag.getFloat("thirst_bar"));
        this.entityData.set(STAMINA, pTag.getFloat("stamina_bar"));
        this.entityData.set(SOCIAL, pTag.getFloat("social_bar"));

        if(pTag.contains("mate_uuid")){
            Optional<UUID> mate_uuid = Optional.of(pTag.getUUID("mate_uuid"));
            this.entityData.set(DINO_MATE, mate_uuid);
        } else this.entityData.set(DINO_MATE, Optional.empty());

        if(pTag.contains("family_uuid")){
            Optional<UUID> mate_uuid = Optional.of(pTag.getUUID("family_uuid"));
            this.entityData.set(DINO_FAMILY_UUID, mate_uuid);
        } else this.entityData.set(DINO_FAMILY_UUID, Optional.empty());

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

    public boolean canTarget(LivingEntity target) {

        if(this.isFamily(target)){
            return false;
        }
        if(this.isBaby()){
            return false;
        }

       return target.getVehicle() != this;
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

    //BRAIN

    @Override
    protected Brain.Provider<Dinosaur> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((block, dinosaur) -> dinosaur.canTargetWaterSource(block));
        waterSourceSensor.setRadius(20);

        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((item, dinosaur) -> dinosaur.canTargetFoodItem(item));
        foodItemSensor.setRadius(20);

        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();


        return List.of(
                waterSourceSensor,
                nearbyLivingEntitySensor,
                foodItemSensor
        );
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
                new MoveToWalkTarget<>().stopIf((entity) -> (entity instanceof Dinosaur dinosaur) && (dinosaur.isResting() || dinosaur.isDrinking() || dinosaur.isDeadOrDying())),
               new SetMateFromNearbyDinosaurs<>(),
            //    new RoarAtThreat<>(34)
//.whenStarting(dinosaur -> dinosaur.entityData.set(IS_ROARING_STATE, true))
                       // .whenStarting(dinosaur -> dinosaur.playSound(SoundInit.TYRANNOSAUR_ROAR_1.get()))
                     //   .whenStopping(dinosaur -> dinosaur.entityData.set(IS_ROARING_STATE,false)),
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
                        new TargetOrRetaliate<>().attackablePredicate(entity -> canTarget(entity))),
                new OneRandomBehaviour<>(
                        new SetRandomWalkTarget<>()
                )
        );
    }
    @Override
    public BrainActivityGroup<? extends Dinosaur> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>()
                        .invalidateIf((entity, target) -> target instanceof Player pl && (pl.isCreative() || pl.isSpectator())),
                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f),
                new AnimatableMeleeAttack<>(20)
                        .whenStarting(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",true))
                        .whenStopping(dinosaur -> DinoAnimationUtils.setAnimationState((Dinosaur) dinosaur,"attack",false))
        );
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1,new BreedGoal(this, 1.0));
    }

    @Override
    public void tick() {
        super.tick();
        if(!level().isClientSide()) {
            this.hungerDrainTick++;
            this.thirstDrainTick++;
            this.socialDrainTick++;
            this.staminaDrainTick++;

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
                this.tickHunger();
                if(this.isHungry() && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_HUNGRY.get())){
                    BrainUtils.setMemory(this, MemoryTypesInit.IS_HUNGRY.get(), true);
                }
                this.hungerDrainTick = 0;
            }
            if(this.thirstDrainTick >= this.getDinoBehaviour().thirstTickRate()){
                this.tickThirst();
                if(this.isThirsty() && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_THIRSTY.get())){
                    BrainUtils.setMemory(this, MemoryTypesInit.IS_THIRSTY.get(), true);
                }
                this.thirstDrainTick = 0;
            }
            if(this.staminaDrainTick >= this.getDinoBehaviour().energyTickRate()){
                this.tickStamina();
                if(this.isTired() && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_TIRED.get())){
                    BrainUtils.setMemory(this, MemoryTypesInit.IS_TIRED.get(), true);
                }
                this.staminaDrainTick = 0;
            }
            if(this.socialDrainTick >= 100){
                this.tickSocial();
                this.socialDrainTick = 0;
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
                .add(AttributesInit.DINO_SOCIAL_NEED.get(),100);
    }

    /// Sets dinosaurs maximum needs values, called by spawn egg. .
    public void setDinoBaseNeeds(DinoBehaviourData data){
        this.getAttribute(AttributesInit.DINO_ENERGY_NEED.get()).setBaseValue(data.energyCapacity());
        this.getAttribute(AttributesInit.DINO_THIRST_NEED.get()).setBaseValue(data.thirstCapacity());
        this.getAttribute(AttributesInit.DINO_HUNGER_NEED.get()).setBaseValue(data.stomachCapacity());
    }

    /// Needs Methods
    public float getMaxHunger(){
        return (float) this.getAttributeValue(AttributesInit.DINO_HUNGER_NEED.get());
    }

    public float getMaxThirst(){
        return (float) this.getAttributeValue(AttributesInit.DINO_THIRST_NEED.get());
    }

    public float getMaxStamina(){
        return (float) this.getAttributeValue(AttributesInit.DINO_ENERGY_NEED.get());
    }
    public boolean isStaminaFull(){
        return this.entityData.get(STAMINA) == this.getMaxStamina();
    }

    public float getMaxSocial(){
        return (float) this.getAttributeValue(AttributesInit.DINO_SOCIAL_NEED.get());
    }

    public boolean isHungry(){
        float currentHunger = this.entityData.get(HUNGER);
        float maxhunger = this.getMaxHunger();
        float lowRiskThreshold = (float) this.getDinoBehaviour().lowRisk();
        float stomachThreshold = maxhunger * lowRiskThreshold;

        return currentHunger < stomachThreshold;
    }

    public boolean isThirsty(){
        return this.entityData.get(THIRST) < this.getMaxThirst() * this.getDinoBehaviour().mediumRisk();
    }

    public boolean isDehydratedOrStarving(){
        return this.entityData.get(THIRST) == 0 || this.entityData.get(HUNGER) == 0;
    }

    public boolean isTired(){
        return this.entityData.get(STAMINA) < this.getMaxStamina() * this.getDinoBehaviour().highRisk();
    }

    public boolean isLonely(){
        return this.entityData.get(SOCIAL) == 0;
    }
    public void setCurrentHunger(float pHunger){
        this.entityData.set(HUNGER, pHunger);
    }
    public void setCurrentThirst(float pThirst){this.entityData.set(THIRST, pThirst);}

    public void tickHunger(){
        float currentHunger = this.entityData.get(HUNGER);
        float hungerDecrease = (float) this.getDinoBehaviour().eatRate();
        float newCurrentValue = currentHunger - hungerDecrease;

        if(currentHunger <= 0){
            this.hurt(damageSources().starve(),0.5f);
        } else {
            if(newCurrentValue <= 0){
                this.entityData.set(HUNGER, 0.0F);
            } else {
                this.entityData.set(HUNGER, newCurrentValue);
            }
        }
    }

    public void tickThirst(){
        float currentThirst = this.entityData.get(THIRST);
        float thirstDecrease = (float) this.getDinoBehaviour().dehydrationRate();
        float newCurrentValue = currentThirst - thirstDecrease;

        if(currentThirst <= 0){
            this.hurt(damageSources().dryOut(),0.5f);
        } else {
            if(newCurrentValue <= 0){
                this.entityData.set(THIRST, 0.0F);
            } else {
                this.entityData.set(THIRST, newCurrentValue);
            }
        }
    }

    public void tickStamina(){
        float currentStamina = this.entityData.get(STAMINA);
        float staminaDecrease = (float) this.getDinoBehaviour().baseExhaustionRate();
        float newCurrentValue;

        if(BrainUtils.hasMemory(this, MemoryTypesInit.IS_RESTING.get())) {
            if(!this.isStaminaFull()) {
                newCurrentValue = currentStamina + 50;
            } else {
                newCurrentValue = this.getMaxStamina();
            }
        } else {
            newCurrentValue = currentStamina - staminaDecrease;
        }

        if (newCurrentValue <= 0) {
            this.entityData.set(STAMINA, 0.0F);
        } else {
            this.entityData.set(STAMINA, newCurrentValue);
        }

    }

    public void tickSocial(){
    }

    public void feed(ItemStack foodItem){

        //for some reason this produces null pointer, supposed to grab food value.
        // double pHungerIncrease = this.getDinoDiet().foodMap().get(foodItem.getDescriptionId());
        float currentHunger = this.entityData.get(HUNGER);
        float maxHunger = this.getMaxHunger();
        float pCurrentHunger = currentHunger + (float) 50;

        this.setCurrentHunger(Math.min(pCurrentHunger, maxHunger));
    }

    public void drink(float pThirstIncrease){
        float currentThirst = this.entityData.get(THIRST);
        float maxThirst = this.getMaxThirst();
        float pCurrentThirst = currentThirst + pThirstIncrease;

        if (pCurrentThirst >= maxThirst){
            this.setCurrentThirst(maxThirst);
        } else this.setCurrentThirst(pCurrentThirst);
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
        return this.entityData.get(DinoAnimationUtils.IS_ROARING_STATE);
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

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        EntityType<?> babyType = this.getDinoData().getBaseDino();
        return (Dinosaur) babyType.create(serverLevel);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        @Nullable Dinosaur dinosaur = (Dinosaur) this.getBreedOffspring(level, mate);
        if(mate instanceof Dinosaur pMate) {
            if (dinosaur != null) {
                dinosaur.setDinoData(this.getDinoData());
                dinosaur.setDinoBehaviour(this.getDinoBehaviour().toNBT(this.getDinoBehaviour()));
                dinosaur.setDinoBaseNeeds(this.getDinoBehaviour());
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
        return this.getAge() < -18000;
    }
    public boolean isJuvanile(){
        return this.getAge() >= -18000 && this.getAge() < -12000;
    }
    public boolean isSubAdult(){
        return this.getAge() >= -12000 && this.getAge() < 0;
    }
    public int getGrowthStage(){
        if(this.isBaby()){
            return 1;
        }
        else if(this.isJuvanile()){
            return 2;
        }
        else if(this.isSubAdult()){
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
        return isMate(otherAnimal.getUUID());
    }
    public boolean canMateWith(Dinosaur pDinosaur, Dinosaur pMate){

        if(pDinosaur.isBaby() || pMate.isBaby()){
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
        return this.entityData.get(DINO_MATE).isPresent();
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
}
