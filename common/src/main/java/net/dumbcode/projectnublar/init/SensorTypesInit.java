package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.sensors.NearestWaterSourceSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearestItemSensor;

import java.util.function.Supplier;

public class SensorTypesInit {
    public static void init(){}

    public static final Supplier<SensorType<NearestWaterSourceSensor<?>>> NEAREST_WATER_SOURCE = register("nearest_drinkable_source_block", NearestWaterSourceSensor::new);


    private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
        return Constants.PN_SBL_LOADER.registerSensorType(id, sensor);
    }
}
