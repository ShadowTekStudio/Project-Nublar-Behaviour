package net.dumbcode.projectnublar.api;

public record DinoBehaviourData(
        String speciesID,
        String dietID,
        String dietType,

        double stomachCapacity,
        double thirstCapacity,

        double eatRate,
        double dehydrationRate,

        int hungerTickRate,
        int thirstTickRate,

        double lowRisk,
        double mediumRisk,
        double highRisk,

        int eating1Delay,
        int drinkingDelay
)
{

}


