package net.dumbcode.projectnublar.api;

import net.minecraft.nbt.CompoundTag;

public record DinoBehaviourData(
        String speciesID,
        String dietID,
        String dietType,

        double maxHealth,
        double stomachCapacity,
        double thirstCapacity,

        double energyCapacity,
        double attack,
        double resistance,

        double speed,
        double size,
        double intelligence,

        double social,
        double groupSize,
        double domesticity,

        double fertility,
        double gestationTime,
        double clutchSize,

        double visionQuality,
        double immunity,
        double tamingScore,

        double aggressionScore,
        double healthRegen,
        double growthRate,

        double socialDrain,
        double trustIncrease,
        double eatRate,

        double dehydrationRate,
        double baseExhaustionRate,

        int hungerTickRate,
        int thirstTickRate,
        int energyTickRate,

        double lowRisk,
        double mediumRisk,
        double highRisk,

        boolean pack_hunter,
        boolean herding,
        boolean nocturnal
)
{

    public static DinoBehaviourData fromNBT(CompoundTag tag) {
        String pSpeciesID = tag.getString("species_id");
        String pDietID = tag.getString("diet_id");
        String pDietType = tag.getString("diet_type");

        double pMaxHealth = tag.getDouble("default_health");
        double pStomachCapacity = tag.getDouble("default_stomach_capacity");
        double pThirstCapacity= tag.getDouble("default_thirst_capacity");

        double pEnergyCapacity= tag.getDouble("default_energy_capacity");
        double pAttack = tag.getDouble("default_attack_damage");
        double pResistance= tag.getDouble("default_resistance");

        double pSpeed= tag.getDouble("default_speed");
        double pSize= tag.getDouble("default_size");
        double pIntelligence= tag.getDouble("default_intelligence");

        double pSocial= tag.getDouble("default_social");
        double pGroupSize= tag.getDouble("default_group_size");
        double pDomesticity= tag.getDouble("default_domesticity");

        double pFertility= tag.getDouble("default_fertility");
        double pGestationTime= tag.getDouble("default_gestation_time");
        double pClutchSize= tag.getDouble("default_egg_clutch");

        double pVisionQuality= tag.getDouble("default_vision");
        double pImmunity= tag.getDouble("default_immunity");
        double pTamingScore= tag.getDouble("default_tame_score");

        double pAggressionScore= tag.getDouble("default_aggression");
        double pHealthRegen= tag.getDouble("default_health_regen");
        double pGrowthRate= tag.getDouble("default_growth_rate");

        double pSocialDrain= tag.getDouble("default_social_drain");
        double pTrustIncrease = tag.getDouble("default_trust_increase");
        double pEatRate= tag.getDouble("default_eat_rate");

        double pDehydrationRate = tag.getDouble("default_dehydration_rate");
        double pBaseExhaustionRate = tag.getDouble("default_exhaustion_rate");
        int pHungerTickRate = tag.getInt("default_hunger_tick_rate");
        int pThirstTickRate = tag.getInt("default_thirst_tick_rate");
        int pEnergyTickRate = tag.getInt("default_energy_tick_rate");
        double pLowRisk = tag.getDouble("low_risk_threshold");
        double pMediumRisk = tag.getDouble("medium_risk_threshold");
        double pHighRisk = tag.getDouble("high_risk_threshold");
        boolean pPack = tag.getBoolean("can_form_pack");
        boolean pHerd = tag.getBoolean("can_form_herd");
        boolean pNocturnal = tag.getBoolean("nocturnal");

        return new DinoBehaviourData(pSpeciesID,pDietID,pDietType,pMaxHealth,pStomachCapacity,pThirstCapacity,pEnergyCapacity,pAttack,pResistance,pSpeed,pSize,
                pIntelligence,pSocial,pGroupSize,pDomesticity,pFertility,pGestationTime,pClutchSize,pVisionQuality,pImmunity,pTamingScore,pAggressionScore,pHealthRegen,pGrowthRate,
                pSocialDrain,pTrustIncrease,pEatRate,pDehydrationRate, pBaseExhaustionRate,pHungerTickRate,pThirstTickRate,pEnergyTickRate,pLowRisk,pMediumRisk,pHighRisk,pPack,pHerd,pNocturnal);
    }

    public CompoundTag toNBT(DinoBehaviourData behaviourData) {
        CompoundTag tag = new CompoundTag();
        tag.putString("species_id", behaviourData.speciesID);
        tag.putString("diet_id", behaviourData.dietID);
        tag.putString("diet_type", behaviourData.dietType);
        tag.putDouble("default_health",behaviourData.maxHealth);
        tag.putDouble("default_stomach_capacity", behaviourData.stomachCapacity);
        tag.putDouble("default_thirst_capacity", behaviourData.thirstCapacity);
        tag.putDouble("default_energy_capacity", behaviourData.energyCapacity);
        tag.putDouble("default_attack_damage",behaviourData.attack);
        tag.putDouble("default_resistance",behaviourData.resistance);
        tag.putDouble("default_speed",behaviourData.speed);
        tag.putDouble("default_size",behaviourData.size);
        tag.putDouble("default_intelligence",behaviourData.intelligence);
        tag.putDouble("default_social",behaviourData.social);
        tag.putDouble("default_group_size",behaviourData.groupSize);
        tag.putDouble("default_domesticity",behaviourData.domesticity);
        tag.putDouble("default_fertility",behaviourData.fertility);
        tag.putDouble("default_gestation_time",behaviourData.gestationTime);
        tag.putDouble("default_egg_clutch",behaviourData.clutchSize);
        tag.putDouble("default_vision",behaviourData.visionQuality);
        tag.putDouble("default_immunity",behaviourData.immunity);
        tag.putDouble("default_tame_score",behaviourData.tamingScore);
        tag.putDouble("default_aggression",behaviourData.aggressionScore);

        tag.putDouble("default_health_regen",behaviourData.healthRegen);
        tag.putDouble("default_growth_rate",behaviourData.growthRate);
        tag.putDouble("default_social_drain",behaviourData.socialDrain);
        tag.putDouble("default_trust_increase",behaviourData.trustIncrease);
        tag.putDouble("default_eat_rate", behaviourData.eatRate);
        tag.putDouble("default_dehydration_rate", behaviourData.dehydrationRate);
        tag.putDouble("default_exhaustion_rate", behaviourData.baseExhaustionRate);
        tag.putInt("default_hunger_tick_rate", behaviourData.hungerTickRate);
        tag.putInt("default_thirst_tick_rate", behaviourData.thirstTickRate);
        tag.putInt("default_energy_tick_rate", behaviourData.energyTickRate);
        tag.putDouble("low_risk_threshold",behaviourData.lowRisk);
        tag.putDouble("medium_risk_threshold",behaviourData.mediumRisk);
        tag.putDouble("high_risk_threshold",behaviourData.highRisk);

        tag.putBoolean("can_form_pack", behaviourData.pack_hunter);
        tag.putBoolean("can_form_herd", behaviourData.herding);
        tag.putBoolean("nocturnal", behaviourData.nocturnal);

        return tag;
    }
}


