package net.dumbcode.projectnublar.entity;


import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.entity.api.FossilRevived;
import net.dumbcode.projectnublar.entity.behaviour.DinosaurEatGoal;
import net.dumbcode.projectnublar.entity.behaviour.IdleAnimationBehaviour;
import net.dumbcode.projectnublar.init.DataSerializerInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class Dinosaur extends PathfinderMob implements FossilRevived, GeoEntity, SmartBrainOwner<Dinosaur> {
    public static EntityDataAccessor<DinoData> DINO_DATA = SynchedEntityData.defineId(Dinosaur.class, DataSerializerInit.DINO_DATA);
    public static EntityDataAccessor<CompoundTag> DINO_BEHAVIOUR = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.COMPOUND_TAG);
    public static EntityDataAccessor<String> DIET_TYPE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.STRING);
    public final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected @Nullable DinoBehaviourData cachedBehaviourData;
    private double currentHunger;
    private int hungerTicker = 0;
    private double baseEatRate;
    private double maxStomachCapacity;
    private int hungerTickRate;
    private double lowRiskThreshold;
    private double mediumRiskThreshold;
    private double highRiskThreshold;

    public Dinosaur(EntityType<? extends PathfinderMob> $$0, Level $$1) {super($$0, $$1);}

    public static final String MAIN_CONTROLLER = "controller";

    public static List<String> idleAnimations = List.of("sniffingair", "sniffground", /*"speak1",*/ "lookleft", "lookright", "scratching", "shakehead", "shakebody");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkController(this));

        for (int i = 0 ;  i < idleAnimations.size(); i++){
            String string = idleAnimations.get(i);
            AnimationController<Dinosaur> controller = new AnimationController<>(this,MAIN_CONTROLLER,5,this::poseIdle);
            controller.triggerableAnim(string,RawAnimation.begin().thenPlay(string));

            controllers.add(controller);
        }

    }
    protected PlayState poseIdle(AnimationState<Dinosaur> state) {
        if (!state.isMoving())
            return PlayState.STOP;
        else return PlayState.CONTINUE;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DINO_DATA, new DinoData());
        this.entityData.define(DINO_BEHAVIOUR, new CompoundTag());
        this.entityData.define(DIET_TYPE, "omnivore");
    }

    public DinoData getDinoData() {
        return this.entityData.get(DINO_DATA);
    }
    public String getDietType() {
        return this.entityData.get(DIET_TYPE);
    }
    public DinoBehaviourData getDinoBehaviour(){
        if(this.cachedBehaviourData == null){
            this.cachedBehaviourData = behaviourFromTag(this.entityData.get(DINO_BEHAVIOUR));
        }
        return cachedBehaviourData;
    }

    @Override
    public void checkDespawn() {}

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35).add(Attributes.MOVEMENT_SPEED, .25)
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("dino_data", this.getDinoData().toNBT());
        tag.put("behaviour_profile", this.entityData.get(DINO_BEHAVIOUR));
        tag.putDouble("current_hunger", this.currentHunger);
        tag.putString("diet_type_string", this.getDietType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pTag) {
        super.readAdditionalSaveData(pTag);
        entityData.set(DINO_DATA, DinoData.fromNBT(pTag.getCompound("dino_data")));

        if(pTag.contains("behaviour_profile")){
            this.entityData.set(DINO_BEHAVIOUR, pTag.getCompound("behaviour_profile"));
            this.getDinoBehaviour();
            this.setHungerValues();
        }
        if (pTag.contains("current_hunger")) {this.currentHunger = pTag.getDouble("current_hunger");}
        if (pTag.contains("diet_type_string")) {setDietType(pTag.getString("diet_type_string"));}
    }

    //To-do: find a better way to do this, it works, I just don't like looking at it :(
    public static CompoundTag behaviourToTag(DinoBehaviourData profile) {
        CompoundTag tag = new CompoundTag();
        tag.putString("species_id", profile.speciesID());
        tag.putString("diet_type", profile.dietType());
        tag.putDouble("default_stomach_capacity", profile.stomachCapacity());
        tag.putDouble("default_eat_rate", profile.eatRate());
        tag.putDouble("default_hunger_tick_rate", profile.tickRate());
        tag.putDouble("low_risk_threshold",profile.lowRisk());
        tag.putDouble("medium_risk_threshold",profile.mediumRisk());
        tag.putDouble("high_risk_threshold",profile.highRisk());

        return tag;
    }
    public static DinoBehaviourData behaviourFromTag(CompoundTag tag){
        String speciesId = tag.getString("species_id");
        String diet_type = tag.getString("diet_type");
        double stomachCapacity = tag.getDouble("default_stomach_capacity");
        double eatRate =  tag.getDouble("default_eat_rate");
        double tickRate =  tag.getDouble("default_hunger_tick_rate");
        double lowRisk = tag.getDouble("low_risk_threshold");
        double mediumRisk = tag.getDouble("low_risk_threshold");
        double highRisk = tag.getDouble("low_risk_threshold");

        return new DinoBehaviourData(speciesId,diet_type,stomachCapacity,eatRate,tickRate,lowRisk,mediumRisk,highRisk);
    }

    public void setHungerValues(){
        if(this.getDinoBehaviour() == null) {
            Constants.LOG.error("Unable set Hunger values for Dinosaur: {} , at location {} because dinoBehaviour is null", this ,this.blockPosition());
        } else {
            double eatRateGene = this.getDinoData().getGeneValue(GeneInit.EAT_RATE.get());
            double stomachGene = this.getDinoData().getGeneValue(GeneInit.STOMACH_CAPACITY.get());

            if (eatRateGene == 0.0) { //incase no gene modifier set
                eatRateGene = 1;
            }
            if (stomachGene == 0.0) { //incase no gene modifier set
                stomachGene = 1;
            }

            this.setDietType(this.getDinoBehaviour().dietType());
            this.baseEatRate = this.getDinoBehaviour().eatRate() * eatRateGene;
            this.maxStomachCapacity = this.getDinoBehaviour().stomachCapacity() * stomachGene;
            this.hungerTickRate = (int) this.getDinoBehaviour().tickRate();
            this.lowRiskThreshold = this.getDinoBehaviour().lowRisk();
            this.mediumRiskThreshold = this.getDinoBehaviour().mediumRisk();
            this.highRiskThreshold = this.getDinoBehaviour().highRisk();
        }
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
    public void setDinoBehaviour(DinoBehaviourData profile){
        this.entityData.set(DINO_BEHAVIOUR, behaviourToTag(profile));
    }
    public void setDietType(String pDietType) {
        this.entityData.set(DIET_TYPE, pDietType);
    }


    @Override
    public List<? extends ExtendedSensor<? extends Dinosaur>> getSensors() {
        NearbyLivingEntitySensor<Dinosaur> nearbyLivingEntitySensor = new NearbyLivingEntitySensor<>();
        nearbyLivingEntitySensor.setPredicate((target, entity) -> canTarget(target));
        return List.of(nearbyLivingEntitySensor, // This tracks nearby entities
                new HurtBySensor<>());
    }

    public boolean canTarget(LivingEntity target) {
        if (target instanceof Dinosaur dinosaur && dinosaur.getDinoData().getBaseDino() == this.getDinoData().getBaseDino())
            return false;
        if (this.getCurrentHunger() > getLowRiskThreshold()) return false;
        if (target instanceof Player player && player.isCreative() && (this.getCurrentHunger() > getMediumRiskThreshold()))
            return false;
        if (target instanceof Monster && this.getCurrentHunger() > this.getHighRiskThreshold()) return false;
        if (target.isDeadOrDying()) {
            return false;
        }
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
                                .attackablePredicate(entity -> canTarget(entity)),// Set the attack target and walk target based on nearby entities
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
                        .invalidateIf((entity, target) -> target instanceof Player pl && (pl.isCreative() || pl.isSpectator())),// Cancel fighting if the target is no longer valid or not worth risk

                new SetWalkTargetToAttackTarget<>().speedMod((owner, target) -> 1.5f),      // Set the walk target to the attack target

                new AnimatableMeleeAttack<>(4)
        ); // Melee attack the target if close enough
    }

    @Override
    public void tick() {
        super.tick();

        hungerTicker++;
        if (this.getDinoBehaviour() == null || this.entityData.get(DINO_BEHAVIOUR).isEmpty()) {
            Constants.LOG.info("Dinosaur Missing Behaviour data, re-assigning. Dinosaur: {} , at Location {}",this.getUUID(),this.blockPosition());
            DinoBehaviourData data = BehaviourDataReloadListener.getBehaviourInfoForDino(this.getType());
            this.setDinoBehaviour(data);
            this.setHungerValues();
        } else {
            if (hungerTicker >= this.getHungerTickRate()) {
                if (this.isStarving()) {
                    //To do add starvation damage to config
                    this.hurt(damageSources().starve(), 0.5F);
                } else this.decreaseHunger(this.getEatRate());
                hungerTicker = 0;
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this)); //cant get out of water otherwise
        this.goalSelector.addGoal(3, new DinosaurEatGoal(this, this.getDietType()));
    }

    //Hunger Stats
    public double getEatRate() {return this.baseEatRate;}
    public double getStomachCapacity() {return this.maxStomachCapacity;}
    public int getHungerTickRate(){return this.hungerTickRate;}

    //Hunger Meter
    public double getCurrentHunger() {return this.currentHunger;}
    public void setCurrentHunger() {this.currentHunger = this.maxStomachCapacity;}

    //Hunting Risk Thresholds
    public double getLowRiskThreshold() {return getStomachCapacity() * (this.lowRiskThreshold);}
    public double getMediumRiskThreshold() {return getStomachCapacity() * (this.mediumRiskThreshold);}
    public double getHighRiskThreshold() {return this.getStomachCapacity() * (this.highRiskThreshold);}

    //Food Methods
    public void feed(double pValue) {this.currentHunger += pValue;}
    public void decreaseHunger(double pValue) {this.currentHunger -= pValue;}
    public boolean isHungry(){return this.currentHunger <= getLowRiskThreshold();}
    public boolean isStarving() {return this.currentHunger <= 0;}


}
