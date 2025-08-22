package net.dumbcode.projectnublar.data;

import com.google.gson.*;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BehaviourDataReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LOCATION = "behaviours";

    private static Map<EntityType<?>, DinoBehaviourData> behaviourDataMap = Collections.emptyMap();

    public BehaviourDataReloadListener(){
        super(GSON, LOCATION);
        Constants.LOG.info("Dino behaviour manager initialized, scanning folder: data/'{}'", LOCATION);

    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<EntityType<?>, DinoBehaviourData> newMap = new HashMap<>();

        for(Map.Entry<ResourceLocation, JsonElement> entry: resourceLocationJsonElementMap.entrySet()){
            ResourceLocation fileID = entry.getKey();
            JsonElement element = entry.getValue();

            try {
                if(!element.isJsonObject()){
                    Constants.LOG.error("Skipping Species Behaviour File, root element not json: '{}' ", fileID);
                    continue;
                }
                JsonObject jsonObject = element.getAsJsonObject();

                String entityIdstring = GsonHelper.getAsString(jsonObject, "species_id");
                ResourceLocation entityRl = ResourceLocation.tryParse(entityIdstring);

                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityRl)
                        .orElseThrow(() -> new JsonSyntaxException("Unknown entity_id" + entityRl + "in DNA extraction file: " + fileID));

                String dietID = GsonHelper.getAsString(jsonObject, "diet_id");
                String dietType = GsonHelper.getAsString(jsonObject, "diet_type");

                double maxHealth = GsonHelper.getAsDouble(jsonObject,"default_health");
                double stomachCapacity = GsonHelper.getAsDouble(jsonObject, "default_stomach_capacity");
                double thirstCapacity = GsonHelper.getAsDouble(jsonObject, "default_thirst_capacity");
                double energyCapacity = GsonHelper.getAsDouble(jsonObject, "default_energy_capacity");
                double attack = GsonHelper.getAsDouble(jsonObject, "default_attack_damage");
                double resistance= GsonHelper.getAsDouble(jsonObject, "default_resistance");

                double speed= GsonHelper.getAsDouble(jsonObject, "default_speed");
                double size= GsonHelper.getAsDouble(jsonObject, "default_size");
                double intelligence= GsonHelper.getAsDouble(jsonObject, "default_intelligence");

                double social = GsonHelper.getAsDouble(jsonObject, "default_social");
                double groupSize = GsonHelper.getAsDouble(jsonObject, "default_group_size");
                double domesticity = GsonHelper.getAsDouble(jsonObject, "default_domesticity");

                double fertility = GsonHelper.getAsDouble(jsonObject, "default_fertility");
                double gestationTime = GsonHelper.getAsDouble(jsonObject, "default_gestation_time");
                double clutchSize = GsonHelper.getAsDouble(jsonObject, "default_egg_clutch");

                double visionQuality = GsonHelper.getAsDouble(jsonObject, "default_vision");
                double immunity = GsonHelper.getAsDouble(jsonObject, "default_immunity");
                double tamingScore = GsonHelper.getAsDouble(jsonObject, "default_tame_score");

                double aggressionScore = GsonHelper.getAsDouble(jsonObject, "default_aggression");
                double healthRegen = GsonHelper.getAsDouble(jsonObject, "default_health_regen");
                double growthRate = GsonHelper.getAsDouble(jsonObject, "default_growth_rate");

                double socialDrain = GsonHelper.getAsDouble(jsonObject, "default_social_drain");
                double trustIncrease = GsonHelper.getAsDouble(jsonObject, "default_trust_increase");

                double eatRate = GsonHelper.getAsDouble(jsonObject, "default_eat_rate");
                double dehydrationRate = GsonHelper.getAsDouble(jsonObject, "default_dehydration_rate");
                double exhaustionRate = GsonHelper.getAsDouble(jsonObject, "default_exhaustion_rate");

                int hungerTickRate = GsonHelper.getAsInt(jsonObject, "default_hunger_tick_rate");
                int thirstTickRate = GsonHelper.getAsInt(jsonObject, "default_thirst_tick_rate");
                int energyTickRate = GsonHelper.getAsInt(jsonObject, "default_energy_tick_rate");

                double lowRisk = GsonHelper.getAsDouble(jsonObject, "low_risk_threshold");
                double mediumRisk = GsonHelper.getAsDouble(jsonObject, "medium_risk_threshold");
                double highRisk = GsonHelper.getAsDouble(jsonObject, "high_risk_threshold");

                boolean pack = GsonHelper.getAsBoolean(jsonObject,"can_form_pack");
                boolean herd = GsonHelper.getAsBoolean(jsonObject,"can_form_herd");
                boolean nocturnal = GsonHelper.getAsBoolean(jsonObject,"nocturnal");

                DinoBehaviourData data = new DinoBehaviourData(
                        entityIdstring,dietID,dietType,maxHealth,
                        stomachCapacity,thirstCapacity,energyCapacity,
                        attack,resistance,speed,size,intelligence,social,groupSize,domesticity,fertility,gestationTime,
                        clutchSize,visionQuality,immunity,tamingScore,aggressionScore,
                        healthRegen,growthRate,socialDrain,trustIncrease,
                        eatRate,dehydrationRate,exhaustionRate,hungerTickRate,
                        thirstTickRate,energyTickRate,lowRisk,
                        mediumRisk,highRisk,pack,herd,nocturnal
                );


                if(newMap.containsKey(entityType)){
                    Constants.LOG.info("Duplicate behaviour assigned, over-writing previous");
                }
                newMap.put(entityType, data);
                Constants.LOG.info("Loaded Behaviour data for: " + entityType);

            } catch (Exception e){
                System.err.println("Failed to parse Behaviour Assignment file: {} - Error: {}" +fileID + e.getMessage());
            }

        }
        behaviourDataMap = newMap;
        System.out.println("Finished Applying behaviour assignment data. Loaded {} valid entries" + behaviourDataMap.size());
    }
    @Nullable
    public static DinoBehaviourData getBehaviourInfoForDino(EntityType<?> speciesID){
        return behaviourDataMap.get(speciesID);
    }
}
