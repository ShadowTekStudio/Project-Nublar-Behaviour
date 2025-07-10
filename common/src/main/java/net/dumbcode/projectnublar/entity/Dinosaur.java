package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.behaviour.DrinkBehaviour;
import net.dumbcode.projectnublar.entity.behaviour.EatBehaviour;
import net.dumbcode.projectnublar.entity.behaviour.RestingBehaviour;
import net.dumbcode.projectnublar.entity.sensors.NearestWaterSourceSensor;
import net.dumbcode.projectnublar.entity.tasks.SetWalkTargetToFoodItem;
import net.dumbcode.projectnublar.entity.tasks.SetWalkTargetToWaterSource;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public abstract class Dinosaur extends PathfinderMob implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur> {
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.COMPOUND_TAG);
    public static EntityDataAccessor<String> DIET_TYPE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.STRING);
    public static EntityDataAccessor<Float> CURRENT_HUNGER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Float> CURRENT_THIRST = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Float> CURRENT_ENERGY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static EntityDataAccessor<Boolean> IS_THIRSTY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_HUNGRY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);
    public static EntityDataAccessor<Boolean> IS_TIRED = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.BOOLEAN);


    protected static final RawAnimation DRINKING_ANIM = RawAnimation.begin().thenPlay("drink");
    protected static final RawAnimation EATING_ANIM = RawAnimation.begin().thenPlay("eat1");
    protected static final RawAnimation REST_ANIM = RawAnimation.begin().thenPlay("rest");
    protected static final RawAnimation REST_IDLE_ANIM = RawAnimation.begin().thenPlay("restidle");
    protected static final RawAnimation GETTING_UP_ANIM = RawAnimation.begin().thenPlay("getup");


    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected @Nullable DinoBehaviourData cachedBehaviourData;

    private DinoDietData dietData;

    private int hungerTicker = 0;
    private int thirstTicker = 0;
    private int energyTicker = 0;
    private int drinkTicks = 0;
    private int eatTicks = 0;
    private int restTicks = 0;

    public Dinosaur(EntityType<? extends PathfinderMob> $$0, Level $$1) {
        super($$0, $$1);

    }

    public static final String RESTING_CONTROLLER = "rest_controller";
    public static final String EAT_CONTROLLER = "eat_controller";
    public static final String DRINK_CONTROLLER = "drink_controller";
    //ANIMATION
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkController(this));
        controllers.add(DefaultAnimations.genericAttackAnimation(this,RawAnimation.begin().thenPlayXTimes("attack1",1)));
        controllers.add(new AnimationController<>(this,DRINK_CONTROLLER, this::poseDrinking)
                .triggerableAnim("drink",DRINKING_ANIM));
        controllers.add(new AnimationController<>(this,EAT_CONTROLLER, this::poseEating)
                .triggerableAnim("eat1",EATING_ANIM)
                .triggerableAnim("eat2",EATING_ANIM));

        controllers.add(new AnimationController<>(this,RESTING_CONTROLLER, this::poseRestIdle)
                .triggerableAnim("rest",REST_ANIM)
                .triggerableAnim("restidle",REST_IDLE_ANIM)
                .triggerableAnim("getup",GETTING_UP_ANIM));
    }

    protected PlayState poseDrinking(AnimationState<Dinosaur> state) {
        PlayState playState;
        if (!state.isMoving() && BrainUtils.hasMemory(this, MemoryTypesInit.IS_DRINKING.get())) {
            playState = PlayState.CONTINUE;
        } else { playState = PlayState.STOP; }
        return playState;
    }
    protected PlayState poseRestIdle(AnimationState<Dinosaur> state) {
        PlayState playState;
        if (!state.isMoving() && BrainUtils.hasMemory(this, MemoryTypesInit.IS_RESTING.get())) {
            playState = PlayState.CONTINUE;
        } else { playState = PlayState.STOP; }
        return playState;
    }
   protected PlayState poseEating(AnimationState<Dinosaur> state) {
       PlayState playState;
       if (!state.isMoving() && BrainUtils.hasMemory(this, MemoryTypesInit.IS_EATING.get())) {
           playState = PlayState.CONTINUE;
       } else {
           playState = PlayState.STOP;
       }
       return playState;
   }




    //MAIN DATA GETTERS
    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }
    public DinoBehaviourData getDinoBehaviour(){
        if(this.cachedBehaviourData == null){
            this.cachedBehaviourData = behaviourFromTag(this.entityData.get(DINO_BEHAVIOUR));
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
        this.entityData.define(DIET_TYPE, "default_Omnivore");
        this.entityData.define(CURRENT_HUNGER, 100.0f);
        this.entityData.define(CURRENT_THIRST, 100.0f);
        this.entityData.define(CURRENT_ENERGY, 100.0f);
        this.entityData.define(IS_HUNGRY, false);
        this.entityData.define(IS_THIRSTY, false);
        this.entityData.define(IS_TIRED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", this.getDinoData().toNBT());
        tag.put("behaviour_profile", this.entityData.get(DINO_BEHAVIOUR));
        tag.putDouble("current_hunger", this.entityData.get(CURRENT_HUNGER));
        tag.putDouble("current_energy", this.entityData.get(CURRENT_ENERGY));
        tag.putString("diet_type_string", this.entityData.get(DIET_TYPE));
        tag.putBoolean("is_hungry", this.entityData.get(IS_HUNGRY));
        tag.putBoolean("is_thirsty", this.entityData.get(IS_THIRSTY));
        tag.putBoolean("is_tired", this.entityData.get(IS_TIRED));


    }

    @Override
    public void readAdditionalSaveData(CompoundTag pTag) {
        super.readAdditionalSaveData(pTag);
        entityData.set(DINO_DATA, DinoData.fromNBT(pTag.getCompound("dino_data")));
        this.entityData.set(DINO_BEHAVIOUR, pTag.getCompound("behaviour_profile"));
        this.entityData.set(CURRENT_HUNGER, (float) pTag.getDouble("current_hunger"));
        this.entityData.set(CURRENT_THIRST,(float) pTag.getDouble("current_thirst"));
        this.entityData.set(CURRENT_ENERGY,(float) pTag.getDouble("current_energy"));
        this.entityData.set(DIET_TYPE, pTag.getString("diet_type_string"));
        this.entityData.set(IS_HUNGRY, pTag.getBoolean("is_hungry"));
        this.entityData.set(IS_THIRSTY, pTag.getBoolean("is_thirsty"));
        this.entityData.set(IS_TIRED, pTag.getBoolean("is_tired"));

    }

    //To-do: find a better way to do this, it works, I just don't like looking at it :( - its just to save reading the json file constantly.
    public static CompoundTag behaviourToTag(DinoBehaviourData profile) {
        CompoundTag tag = new CompoundTag();
        tag.putString("species_id", profile.speciesID());
        tag.putString("diet_id", profile.dietID());
        tag.putString("diet_type", profile.dietType());
        tag.putDouble("default_stomach_capacity", profile.stomachCapacity());
        tag.putDouble("default_thirst_capacity", profile.thirstCapacity());
        tag.putDouble("default_energy_capacity", profile.energyCapacity());
        tag.putDouble("default_eat_rate", profile.eatRate());
        tag.putDouble("default_dehydration_rate", profile.dehydrationRate());
        tag.putDouble("default_exhaustion_rate", profile.baseExhaustionRate());
        tag.putInt("default_hunger_tick_rate", profile.hungerTickRate());
        tag.putInt("default_thirst_tick_rate", profile.thirstTickRate());
        tag.putInt("default_energy_tick_rate", profile.energyTickRate());
        tag.putDouble("low_risk_threshold",profile.lowRisk());
        tag.putDouble("medium_risk_threshold",profile.mediumRisk());
        tag.putDouble("high_risk_threshold",profile.highRisk());
        tag.putInt("eating1_anim_delay", profile.eating1Delay());
        tag.putInt("drinking_anim_delay", profile.drinkingDelay());

        return tag;
    }
    public static DinoBehaviourData behaviourFromTag(CompoundTag tag){
        String speciesId = tag.getString("species_id");
        String dietID = tag.getString("diet_id");
        String diet_type = tag.getString("diet_type");
        double stomachCapacity = tag.getDouble("default_stomach_capacity");
        double thirstCapacity = tag.getDouble("default_thirst_capacity");
        double energyCapacity = tag.getDouble("default_energy_capacity");
        double eatRate =  tag.getDouble("default_eat_rate");
        double dehydrationRate =  tag.getDouble("default_dehydration_rate");
        double exhaustionRate =  tag.getDouble("default_exhaustion_rate");
        int hungerTick =  tag.getInt("default_hunger_tick_rate");
        int thirstTick =  tag.getInt("default_thirst_tick_rate");
        int energyTick =  tag.getInt("default_energy_tick_rate");
        double lowRisk = tag.getDouble("low_risk_threshold");
        double mediumRisk = tag.getDouble("low_risk_threshold");
        double highRisk = tag.getDouble("low_risk_threshold");
        int eating1Delay = tag.getInt("eating1_anim_delay");
        int drinkingDelay = tag.getInt("drinking_anim_delay");

        return new DinoBehaviourData(speciesId,dietID,diet_type,stomachCapacity,thirstCapacity,energyCapacity,eatRate,dehydrationRate,exhaustionRate,
                hungerTick,thirstTick,energyTick,lowRisk,mediumRisk,highRisk, eating1Delay, drinkingDelay);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    //MAIN DATA SETTERS
    public void setDinoData(DinoData dinoData) {
        this.entityData.set(DINO_DATA, dinoData);
    }
    public void setDinoBehaviour(DinoBehaviourData profile){
        this.entityData.set(DINO_BEHAVIOUR, behaviourToTag(profile));
    }
    public void setDietID(String pDietType) {
        this.entityData.set(DIET_TYPE, pDietType);
    }

    //TARGETING BOOLEANS FOR BRAIN
    public boolean canTargetWaterSource(BlockState entity){
           return entity.is(Blocks.WATER);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        if(this.isSleeping()){
            return false;
        }
        DinoDietData validfood = DietReloadListener.getDietInfoForType(this.getDinoBehaviour().dietID());
        return validfood.foodMap().containsKey(stack.getDescriptionId()) && this.isHungry();
    }

    public boolean canTarget(LivingEntity target) {
        if(this.isSleeping()){
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

    //BRAIN

    @Override
    protected Brain.Provider<Dinosaur> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.tickBrain(this);

        if(BrainUtils.hasMemory(this, MemoryTypesInit.IS_DRINKING.get())) {
            if (drinkTicks == 0) {
                this.triggerAnim("drink_controller", "drink");
                this.setCurrentThurstToMax();
            }
            drinkTicks++;
            if (drinkTicks > 0 && drinkTicks < 183) {
                this.getNavigation().stop();
            }
            if (drinkTicks >= 183) {
                BrainUtils.clearMemory(this, MemoryTypesInit.IS_DRINKING.get());
                BrainUtils.clearMemory(this, MemoryTypesInit.IS_THIRSTY.get());
                BrainUtils.clearMemory(this, MemoryModuleType.LOOK_TARGET);
                BrainUtils.clearMemory(this, MemoryModuleType.WALK_TARGET);
                BrainUtils.clearMemory(this, MemoryTypesInit.HAS_FOUND_WATER.get());
                drinkTicks = 0;

            }
        }

           if(BrainUtils.hasMemory(this,MemoryTypesInit.IS_RESTING.get())){
               this.getNavigation().stop();
               if(restTicks == 0){
                   this.triggerAnim(RESTING_CONTROLLER,"rest");
               } else if (restTicks >= 69){
                   this.triggerAnim(RESTING_CONTROLLER, "restidle");
               }
               restTicks++;
               if(restTicks > 200) {
                   this.setCurrentEnergyToMax();
                   BrainUtils.clearMemory(this, MemoryTypesInit.IS_TIRED.get());
                   BrainUtils.clearMemory(this,MemoryTypesInit.IS_RESTING.get());
                   this.triggerAnim(RESTING_CONTROLLER,"getup");
                   restTicks = 0;
               }

           }



        if(BrainUtils.hasMemory(this, MemoryTypesInit.IS_EATING.get())) {
            if(eatTicks == 0){
                this.triggerAnim("eat_controller","eat1");
               this.setCurrentHungerToMax();
            }
            eatTicks++;
            if(eatTicks > 0 && eatTicks < 90){
                this.getNavigation().stop();
            }
           if (eatTicks >= 90) {
               @Nullable ItemStack foodItem;
               if(BrainUtils.hasMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
                   foodItem = BrainUtils.getMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).getItem();
                   if (foodItem != null) {
                       foodItem.shrink(1);
                   }
               }
                BrainUtils.clearMemory(this, MemoryTypesInit.IS_EATING.get());
                BrainUtils.clearMemory(this, MemoryTypesInit.IS_HUNGRY.get());
                BrainUtils.clearMemory(this, MemoryModuleType.LOOK_TARGET);
                BrainUtils.clearMemory(this, MemoryModuleType.WALK_TARGET);
                BrainUtils.clearMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
                eatTicks = 0;

           }

        }

    }
    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearestWaterSourceSensor<Dinosaur> waterSourceSensor = new NearestWaterSourceSensor<>();
        waterSourceSensor.setPredicate((block, dinosaur) -> dinosaur.canTargetWaterSource(block));
        waterSourceSensor.setRadius(20);

        NearestItemSensor<Dinosaur> foodItemSensor = new NearestItemSensor<>();
        foodItemSensor.setPredicate((item, dinosaur) -> dinosaur.canTargetFoodItem(item));
        foodItemSensor.setRadius(20);

        NearbyLivingEntitySensor<Dinosaur> preyTargetFinder = new NearbyLivingEntitySensor<>();
        preyTargetFinder.setPredicate((entity, dinosaur)-> dinosaur.canTarget(entity));


        return List.of(
                waterSourceSensor,
                preyTargetFinder,
                foodItemSensor
        );
    }

    @Override
    public BrainActivityGroup<? extends Dinosaur> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>(),
               new DrinkBehaviour<>(),
                new EatBehaviour<>(),
                new RestingBehaviour<>()
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
        );
    }

    @Override
    public void tick() {
        super.tick();
        if(!level().isClientSide()) {
           hungerTicker++;
           thirstTicker++;
           energyTicker++;
            if (hungerTicker >= this.getHungerTickRate()) {
                System.err.println("Dinosaur Needs Status: " + this.getDinoData().getBaseDino() + ", " + this.getUUID());
                System.err.println("Hunger Status: " + this.getCurrentHunger());
                System.err.println("Thirst Status: " + this.getCurrentThirst());
                System.err.println("Energy Status: " + this.getCurrentEnergy());
                System.err.println("Hunger Depletion: " + this.getEatRate());
                System.err.println("Thirst Depletion: " + this.getDehydrationRate());
                System.err.println("Energy Depletion: " + this.getExhaustionRate());
                if (this.isStarving()) {
                    this.hurt(damageSources().starve(), 0.5f);
                } else {
                    this.decreaseHunger(this.getEatRate());
                }
                hungerTicker = 0;

            }
            if (thirstTicker >= this.getThirstTickrate()) {
                if (this.isDehydrated()) {
                    this.hurt(damageSources().dryOut(), 0.5f);
                } else {
                    this.decreaseThirst(this.getDehydrationRate());
                }
                thirstTicker = 0;
            }
            //Temporary until later implementations of behaviour
            if(energyTicker >= this.getEnergyTickRate()) {
                if(this.isExhausted()){

                } else if (!BrainUtils.hasMemory(this,MemoryTypesInit.IS_RESTING.get())){
                        this.decreaseEnergy(this.getExhaustionRate());
                }
                energyTicker = 0;
            }

            //Telling the brain that a need must be met.
           if(this.isThirsty() && !BrainUtils.hasMemory(this,MemoryTypesInit.IS_THIRSTY.get())){
               BrainUtils.setMemory(this,MemoryTypesInit.IS_THIRSTY.get(), true);
           } else if (!this.isThirsty() && BrainUtils.hasMemory(this,MemoryTypesInit.IS_THIRSTY.get())){
               BrainUtils.clearMemory(this,MemoryTypesInit.IS_THIRSTY.get());
           }

           if(this.isHungry() && !BrainUtils.hasMemory(this,MemoryTypesInit.IS_HUNGRY.get())){
               BrainUtils.setMemory(this,MemoryTypesInit.IS_HUNGRY.get(), true);
           } else if (!this.isHungry() && BrainUtils.hasMemory(this,MemoryTypesInit.IS_HUNGRY.get())){
               BrainUtils.clearMemory(this,MemoryTypesInit.IS_HUNGRY.get());
           }

            if(this.isTired() && !BrainUtils.hasMemory(this, MemoryTypesInit.IS_TIRED.get())){
                BrainUtils.setMemory(this, MemoryTypesInit.IS_TIRED.get(), true);
            } else if (!this.isTired() && BrainUtils.hasMemory(this,MemoryTypesInit.IS_TIRED.get())){
                BrainUtils.clearMemory(this,MemoryTypesInit.IS_TIRED.get());
            }


        }
    }

    //Getters for behaviour

    public float getEatRate() {return (float) this.entityData.get(DINO_BEHAVIOUR).getDouble("default_eat_rate");}

    public float getStomachCapacity() {   return (float) this.entityData.get(DINO_BEHAVIOUR).getDouble("default_stomach_capacity");}

    public int getHungerTickRate(){return this.entityData.get(DINO_BEHAVIOUR).getInt("default_hunger_tick_rate");}

    public float getCurrentHunger() {return this.entityData.get(CURRENT_HUNGER);}

    public void setCurrentHungerToMax() {this.entityData.set(CURRENT_HUNGER, this.getStomachCapacity());}

    public void feed(float pValue) {this.entityData.set(CURRENT_HUNGER, this.getCurrentHunger() + pValue);}

    public void decreaseHunger(float pValue) {
        float currentHunger = this.getCurrentHunger();
        currentHunger -= pValue;
        this.entityData.set(CURRENT_HUNGER, currentHunger);
    }

    public boolean isHungry(){return this.getCurrentHunger() < this.getStomachCapacity() - 20;}

    public boolean isStarving() {return this.entityData.get(CURRENT_HUNGER) <= 0.0;}

    public int getEating1AnimTickDelay(){return this.getDinoBehaviour().eating1Delay();}


    public float getDehydrationRate(){
            return (float) this.getDinoBehaviour().dehydrationRate();
    }

    public float getMaxThirstCapacity(){
        return (float) this.getDinoBehaviour().thirstCapacity();
    }

    public int getThirstTickrate(){
        return this.getDinoBehaviour().thirstTickRate();
    }

    public float getCurrentThirst(){
        return this.entityData.get(CURRENT_THIRST);
    }

    public void setCurrentThurstToMax(){
        this.entityData.set(CURRENT_THIRST, (float) this.getDinoBehaviour().thirstCapacity());
    }

    public void decreaseThirst(float pValue){
        this.entityData.set(CURRENT_THIRST, this.getCurrentThirst() - pValue);
    }
    public float getEnergyCapacity(){
        return (float) this.getDinoBehaviour().energyCapacity();
    }

    public float getCurrentEnergy(){
        return this.entityData.get(CURRENT_ENERGY);
    }
    public float getExhaustionRate(){
        return (float) this.getDinoBehaviour().baseExhaustionRate();
    }

    public void setCurrentEnergyToMax(){
        this.entityData.set(CURRENT_ENERGY,(float) this.getDinoBehaviour().energyCapacity());
    }
    public void increaseEnergy(float pIncrease){
        float newEnergyValue = this.getCurrentEnergy() + pIncrease;
        this.entityData.set(CURRENT_ENERGY, newEnergyValue);
    }
   public void decreaseEnergy(float pDecrease){
        float newEnergyvalue = this.getCurrentEnergy() - pDecrease;
        this.entityData.set(CURRENT_ENERGY, newEnergyvalue);
    }
    public int getEnergyTickRate(){
        return this.getDinoBehaviour().energyTickRate();
    }

    public boolean isThirsty(){
        return this.getCurrentThirst() < this.getMaxThirstCapacity()-20;
    }
    public boolean isDehydrated(){
        return this.entityData.get(CURRENT_THIRST) <= 0;
    }
    public boolean isExhausted(){
        return this.entityData.get(CURRENT_ENERGY) <= 0;
    }
    public boolean isTired(){
        float energeryCapacity = (float) this.entityData.get(DINO_BEHAVIOUR).getDouble("energy_capacity");
        return this.getCurrentEnergy() < (energeryCapacity * 0.5) || this.getCurrentEnergy() == 0;
    }

    //ATTRIBUTES
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35).add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.ARMOR, 2).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
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


}
