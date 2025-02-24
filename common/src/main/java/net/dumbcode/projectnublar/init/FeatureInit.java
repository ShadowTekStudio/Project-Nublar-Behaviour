package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.worldgen.feature.AmberFeature;
import net.dumbcode.projectnublar.worldgen.feature.FossilConfiguration;
import net.dumbcode.projectnublar.worldgen.feature.FossilFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FeatureInit {
    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Constants.MODID, Registries.FEATURE);
    public static DeferredSupplier<Feature<FossilConfiguration>> FOSSIL_FEATURE = FEATURES.register("fossil_feature", () -> new FossilFeature(FossilConfiguration.CODEC));
    public static DeferredSupplier<Feature<FossilConfiguration>> AMBER_FEATURE = FEATURES.register("amber_feature", () -> new AmberFeature(FossilConfiguration.CODEC));

    public static void loadClass() {
    }
}