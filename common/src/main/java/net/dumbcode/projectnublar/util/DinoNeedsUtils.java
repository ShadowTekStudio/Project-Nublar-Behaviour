package net.dumbcode.projectnublar.util;

import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.entity.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.HerbivoreDinosaur;
import net.dumbcode.projectnublar.entity.PackEntity;
import net.dumbcode.projectnublar.init.AttributesInit;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.GeneInit;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class DinoNeedsUtils {
    public static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> THIRST = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SOCIAL = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> AGGRESSION = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> FERTILITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DOMESTICITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> INTELLIGENCE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VISION = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> IMMUNITY = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> TAMING_SCORE = SynchedEntityData.defineId(Dinosaur.class, EntityDataSerializers.FLOAT);

    public static float getMaxHunger(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_HUNGER_NEED.get());}
    public static float getMaxThirst(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_THIRST_NEED.get());}
    public static float getMaxStamina(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_ENERGY_NEED.get());}
    public static float getMaxSocial(Dinosaur dinosaur){return (float) dinosaur.getAttributeValue(AttributesInit.DINO_SOCIAL_NEED.get());}

    public static float getAggressionScoreFromStats(Dinosaur dinosaur){
        double baseAggression = dinosaur.getAttributeValue(AttributesInit.DINO_AGGRESSION.get());
        double multiplier = dinosaur.getDinoData().getGeneValue(GeneInit.AGGRESSION.get());
        if(multiplier != 0.0D){
            double finalScore = baseAggression * (1.0D + (multiplier/100));
            return (float) finalScore;
        } else return (float) baseAggression;
    }

    public static void setAggressionScoreFromStats(Dinosaur dinosaur){
        float aggressionScore = DinoNeedsUtils.getAggressionScoreFromStats(dinosaur);
        dinosaur.getEntityData().set(AGGRESSION, aggressionScore);
    }

    public static float getThreatScore(LivingEntity entity){
        AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttribute = entity.getAttribute(Attributes.ARMOR);
        double attack = 1;
        if(attackAttribute != null){
            attack = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        double defence = 1;
        if(armorAttribute != null) {
            defence = entity.getAttributeValue(Attributes.ARMOR);
        }
        double health = entity.getHealth();
        double speed = entity.getSpeed();

        int groupsize = 1;

        if(entity instanceof CarnivoreDinosaur carnivore){
            if(carnivore.hasPack()){
                @Nullable PackEntity packEntity = carnivore.getPackEntity();
                if(packEntity != null) {
                    groupsize = packEntity.getEntityData().get(PackEntity.DINO_PACK_MEMBERS).size();
                }
            }
        }
        return (float) ((attack * 2) + (defence * 1.7) + (health * 1.4) + (speed * 0.3) ) * groupsize;
    }
    public static boolean isTargetInsideEnclosure(LivingEntity target, Dinosaur dinosaur) {
        BlockPos targetPos = target.blockPosition();
        BlockPos mobPos = dinosaur.blockPosition();

        // Simple: check if there’s a fence in a straight line between mob and target
        Vec3 dir = target.position().subtract(dinosaur.position());
        int steps = (int) dir.length();
        for (int i = 0; i <= steps; i++) {
            Vec3 pCheckPos = dinosaur.position().add(dir.scale(i / (double) steps));
            BlockPos pos = new BlockPos((int) pCheckPos.x,(int) pCheckPos.y,(int) pCheckPos.z);
            BlockState state = dinosaur.level().getBlockState(pos);
            @Nullable BlockEntity entity = null;
            if(dinosaur.level().getBlockEntity(pos) != null){
            entity = dinosaur.level().getBlockEntity(pos);
            }
            if (state.is(BlockInit.ELECTRIC_FENCE.get())||state.is(BlockInit.HIGH_SECURITY_ELECTRIC_FENCE_POST.get()) || state.is(BlockInit.LOW_SECURITY_ELECTRIC_FENCE_POST.get())||
                    (entity != null && entity.equals(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get()))) {
                return false; // target is outside
            }
        }
        return true; // no fence in the way → inside
    }

    public static double getHuntTargetValue(LivingEntity huntTarget){
        double targetFoodValue = 50.0D; //Need to set up config for this

        if(huntTarget instanceof Monster){
            return 0.0;
        }
        if(huntTarget instanceof ServerPlayer){
            targetFoodValue = 200.0D;
        }
        double multiplyerFromRisk = 100.0D / (double) DinoNeedsUtils.getThreatScore(huntTarget);
        return targetFoodValue * multiplyerFromRisk;
    }

    public static boolean isStaminaFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == DinoNeedsUtils.getMaxStamina(dinosaur);}
    public static boolean isHungerFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER) == DinoNeedsUtils.getMaxHunger(dinosaur);}
    public static boolean isThirstFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == DinoNeedsUtils.getMaxThirst(dinosaur);}
    public static boolean isSocialFull(Dinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL) == DinoNeedsUtils.getMaxSocial(dinosaur);}


    public static boolean isHungry(Dinosaur dinosaur){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        float maxhunger = DinoNeedsUtils.getMaxHunger(dinosaur);
        float lowRiskThreshold = (float) dinosaur.getDinoBehaviour().lowRisk();
        float stomachThreshold = maxhunger * lowRiskThreshold;

        return currentHunger < stomachThreshold;
    }
    public static boolean isThirsty(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) < DinoNeedsUtils.getMaxThirst(dinosaur) * dinosaur.getDinoBehaviour().mediumRisk();}
    public static boolean isTired(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) < DinoNeedsUtils.getMaxStamina(dinosaur) * dinosaur.getDinoBehaviour().highRisk();}
    public static boolean isSociallyLow(Dinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL) == 0.0F;}

    public static boolean allNeedsAtZero(Dinosaur dinosaur){
        return dinosaur.getEntityData().get(THIRST) == 0.0 && dinosaur.getEntityData().get(HUNGER) == 0.0F
                && dinosaur.getEntityData().get(SOCIAL) == 0.0 && dinosaur.getEntityData().get(STAMINA) == 0.0F;
    }
    public static boolean isDehydratedOrStarving(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == 0.0F || dinosaur.getEntityData().get(HUNGER) == 0.0F;}
    public static boolean starving(Dinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER) == 0.0F;}
    public static boolean dehyrdrated(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST) == 0.0F;}
    public static boolean isExhausted(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == 0.0F;}
    public static boolean isSociallyDrained(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA) == 0.0F;}


    public static void setCurrentHunger(Dinosaur dinosaur, float pHunger){dinosaur.getEntityData().set(HUNGER, pHunger);}
    public static void setCurrentThirst(Dinosaur dinosaur, float pThirst){dinosaur.getEntityData().set(THIRST, pThirst);}
    public static void setCurrentSocial(Dinosaur dinosaur, float pSocial){dinosaur.getEntityData().set(SOCIAL, pSocial);}
    public static void setCurrentStamina(Dinosaur dinosaur, float pStamina){dinosaur.getEntityData().set(STAMINA, pStamina);}

    public static float getCurrentHunger(Dinosaur dinosaur){return dinosaur.getEntityData().get(HUNGER);}
    public static float getCurrentThirst(Dinosaur dinosaur){return dinosaur.getEntityData().get(THIRST);}
    public static float getCurrentSocial(Dinosaur dinosaur){return dinosaur.getEntityData().get(SOCIAL);}
    public static float getCurrentStamina(Dinosaur dinosaur){return dinosaur.getEntityData().get(STAMINA);}

    public static void tickHunger(Dinosaur dinosaur){
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        float hungerDecrease = (float) dinosaur.getDinoBehaviour().eatRate();
        float newCurrentValue = currentHunger - hungerDecrease;

        if(currentHunger <= 0.0F){
            dinosaur.hurt(dinosaur.damageSources().starve(),0.5f);
        } else {
            if(newCurrentValue <= 0.0F){
                dinosaur.getEntityData().set(HUNGER, 0.0F);
            } else {
                dinosaur.getEntityData().set(HUNGER, newCurrentValue);
            }
        }
    }
    public static void tickThirst(Dinosaur dinosaur){
        float currentThirst = dinosaur.getEntityData().get(THIRST);
        float thirstDecrease = (float) dinosaur.getDinoBehaviour().dehydrationRate();
        float newCurrentValue = currentThirst - thirstDecrease;

        if(currentThirst <= 0.0F){
            dinosaur.hurt(dinosaur.damageSources().dryOut(),0.5f);
        } else {
            if(newCurrentValue <= 0.0F){
                dinosaur.getEntityData().set(THIRST, 0.0F);
            } else {
                dinosaur.getEntityData().set(THIRST, newCurrentValue);
            }
        }
    }
    public static void setDinoBaseNeeds(Dinosaur dinosaur, DinoBehaviourData data){
        dinosaur.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.maxHealth());
  //      dinosaur.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.speed());
        dinosaur.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.attack());
        dinosaur.getAttribute(Attributes.ARMOR).setBaseValue(data.resistance());
     //   dinosaur.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(data.visionQuality());
        dinosaur.getAttribute(AttributesInit.DINO_ENERGY_NEED.get()).setBaseValue(data.energyCapacity());
        dinosaur.getAttribute(AttributesInit.DINO_THIRST_NEED.get()).setBaseValue(data.thirstCapacity());
        dinosaur.getAttribute(AttributesInit.DINO_HUNGER_NEED.get()).setBaseValue(data.stomachCapacity());
        dinosaur.getAttribute(AttributesInit.DINO_SOCIAL_NEED.get()).setBaseValue(data.stomachCapacity());
        dinosaur.getAttribute(AttributesInit.TRUST_SCORE.get()).setBaseValue(data.tamingScore());
        dinosaur.getAttribute(AttributesInit.DINO_VISION.get()).setBaseValue(data.visionQuality());
        dinosaur.getAttribute(AttributesInit.DINO_AGGRESSION.get()).setBaseValue(data.aggressionScore());
        dinosaur.getAttribute(AttributesInit.DINO_INTELLIGENCE.get()).setBaseValue(data.aggressionScore());
        dinosaur.getAttribute(AttributesInit.DINO_FERTILITY.get()).setBaseValue(data.fertility());
        dinosaur.getAttribute(AttributesInit.DINO_IMMUNITY.get()).setBaseValue(data.fertility());
        DinoNeedsUtils.setAggressionScoreFromStats(dinosaur);
    }

    public static void tickStamina(Dinosaur dinosaur){
        float currentStamina = dinosaur.getEntityData().get(STAMINA);
        float staminaDecrease = (float) dinosaur.getDinoBehaviour().baseExhaustionRate();
        float newCurrentValue;

        if(BrainUtils.hasMemory(dinosaur, MemoryTypesInit.IS_RESTING.get())) {
            if(!DinoNeedsUtils.isStaminaFull(dinosaur)) {
                newCurrentValue = currentStamina + 50.0F;
            } else {
                newCurrentValue = DinoNeedsUtils.getMaxStamina(dinosaur);
            }
        } else {
            newCurrentValue = currentStamina - staminaDecrease;
        }

        if (newCurrentValue <= 0) {
            dinosaur.getEntityData().set(STAMINA, 0.0F);
        } else {
            dinosaur.getEntityData().set(STAMINA, newCurrentValue);
        }

    }

    public static void tickSocial(){
    }

    public static void feed(Dinosaur dinosaur, ItemStack foodItem){

        //for some reason this produces null pointer, supposed to grab food value.
        // double pHungerIncrease = this.getDinoDiet().foodMap().get(foodItem.getDescriptionId());
        float currentHunger = dinosaur.getEntityData().get(HUNGER);
        float maxHunger = DinoNeedsUtils.getMaxHunger(dinosaur);
        float pCurrentHunger = currentHunger + 50.0F;

        DinoNeedsUtils.setCurrentHunger(dinosaur, Math.min(pCurrentHunger, maxHunger));
    }

    public static void drink(Dinosaur dinosaur, float pThirstIncrease){
        float currentThirst = dinosaur.getEntityData().get(THIRST);
        float maxThirst = DinoNeedsUtils.getMaxThirst(dinosaur);
        float pCurrentThirst = currentThirst + pThirstIncrease;

        DinoNeedsUtils.setCurrentThirst(dinosaur, Math.min(pCurrentThirst, maxThirst));
    }

}
