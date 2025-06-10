package net.dumbcode.projectnublar.api;

import java.util.Map;

public record DinoDietData(
        String dietType,
        Map<String, Double> foodMap
) {

}
