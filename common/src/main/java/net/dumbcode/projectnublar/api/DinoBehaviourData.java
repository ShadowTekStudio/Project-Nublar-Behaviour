package net.dumbcode.projectnublar.api;

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

}


