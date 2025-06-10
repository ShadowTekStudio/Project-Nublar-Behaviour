package net.dumbcode.projectnublar.api;

public record DinoBehaviourData(
        String speciesID,
        String dietType,
        double stomachCapacity,
        double eatRate,
        double tickRate,
        double lowRisk,
        double mediumRisk,
        double highRisk
)
{

}


