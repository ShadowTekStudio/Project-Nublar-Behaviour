package net.dumbcode.projectnublar.api;

import net.minecraft.nbt.CompoundTag;

public record DinoBehaviourData(
        String speciesID,
        String dietID,
        String dietType,

        double stomachCapacity,
        double thirstCapacity,
        double energyCapacity,

        double eatRate,
        double dehydrationRate,
        double baseExhaustionRate,

        int hungerTickRate,
        int thirstTickRate,
        int energyTickRate,

        double lowRisk,
        double mediumRisk,
        double highRisk,

        int eating1Delay,
        int drinkingDelay
)
{

    public static DinoBehaviourData fromNBT(CompoundTag tag) {
        String pSpeciesID = tag.getString("species_id");
        String pDietID = tag.getString("diet_id");
        String pDietType = tag.getString("diet_type");
        double pStomachCapacity = tag.getDouble("default_stomach_capacity");
        double pThirstCapacity= tag.getDouble("default_thirst_capacity");
        double pEnergyCapacity= tag.getDouble("default_energy_capacity");
        double pEatRate= tag.getDouble("default_eat_rate");
        double pDehydrationRate = tag.getDouble("default_dehydration_rate");
        double pBaseExhaustionRate = tag.getDouble("default_exhaustion_rate");
        int pHungerTickRate = tag.getInt("default_hunger_tick_rate");
        int pThirstTickRate = tag.getInt("default_thirst_tick_rate");
        int pEnergyTickRate = tag.getInt("default_energy_tick_rate");
        double pLowRisk = tag.getDouble("low_risk_threshold");
        double pMediumRisk = tag.getDouble("medium_risk_threshold");
        double pHighRisk = tag.getDouble("high_risk_threshold");
        int pEating1Delay = tag.getInt("eating1_anim_delay");
        int pDrinkingDelay = tag.getInt("drinking_anim_delay");

        return new DinoBehaviourData(pSpeciesID,pDietID,pDietType,pStomachCapacity,pThirstCapacity,pEnergyCapacity,pEatRate,pDehydrationRate,
                pBaseExhaustionRate,pHungerTickRate,pThirstTickRate,pEnergyTickRate,pLowRisk,pMediumRisk,pHighRisk,pEating1Delay,pDrinkingDelay);
    }

    public CompoundTag toNBT(DinoBehaviourData behaviourData) {
        CompoundTag tag = new CompoundTag();
        tag.putString("species_id", behaviourData.speciesID);
        tag.putString("diet_id", behaviourData.dietID);
        tag.putString("diet_type", behaviourData.dietType);
        tag.putDouble("default_stomach_capacity", behaviourData.stomachCapacity);
        tag.putDouble("default_thirst_capacity", behaviourData.thirstCapacity);
        tag.putDouble("default_energy_capacity", behaviourData.energyCapacity);
        tag.putDouble("default_eat_rate", behaviourData.eatRate);
        tag.putDouble("default_dehydration_rate", behaviourData.dehydrationRate);
        tag.putDouble("default_exhaustion_rate", behaviourData.baseExhaustionRate);
        tag.putInt("default_hunger_tick_rate", behaviourData.hungerTickRate);
        tag.putInt("default_thirst_tick_rate", behaviourData.thirstTickRate);
        tag.putInt("default_energy_tick_rate", behaviourData.energyTickRate);
        tag.putDouble("low_risk_threshold",behaviourData.lowRisk);
        tag.putDouble("medium_risk_threshold",behaviourData.mediumRisk);
        tag.putDouble("high_risk_threshold",behaviourData.highRisk);
        tag.putInt("eating1_anim_delay", behaviourData.eating1Delay);
        tag.putInt("drinking_anim_delay", behaviourData.drinkingDelay);

        return tag;
    }
}


