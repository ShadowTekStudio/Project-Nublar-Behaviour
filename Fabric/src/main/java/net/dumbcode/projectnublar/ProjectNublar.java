package net.dumbcode.projectnublar;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.entity.ik.util.ChainCommonClass;
import net.dumbcode.projectnublar.ik.command.TestCommand;
import net.dumbcode.projectnublar.init.EntityInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.fml.config.ModConfig;

import java.util.HashMap;
import java.util.List;

public class ProjectNublar implements ModInitializer {

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ChainCommonClass.isDev = true;
            ChainCommonClass.shouldRenderDebugLegs = true;
            ChainCommonClass.LOGGER.info("Started in a development environment. Debug renderers will be activated by default.");
            TestCommand.register();
        }

        ForgeConfigRegistry.INSTANCE.register(Constants.MODID, ModConfig.Type.COMMON, FossilsConfig.CONFIG_SPEC);
        CommonClass.init();
        EntityInit.attributeSuppliers.forEach(
                p -> FabricDefaultAttributeRegistry.register(p.entityTypeSupplier().get(), p.factory().get().add(Attributes.MOVEMENT_SPEED, 0.23).build())
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                Constants.FOSSIL_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                Constants.AMBER_PLACED
        );
        FossilsConfig.getFossils().forEach((type, fossil) -> {
            List<String> periods = fossil.getPeriods().get();
            List<String> biomes = fossil.getBiomes().get();
            SimpleWeightedRandomList.Builder<FossilPiece> blockStates = new SimpleWeightedRandomList.Builder<>();
            FossilsConfig.Set set = FossilsConfig.getSet(fossil.getPieces().get());
            for (int i = 0; i < set.pieces().get().size(); i++) {
                String piece = set.pieces().get().get(i);
                int weight = set.weights().get().get(i);
                blockStates.add(FossilPieces.getPieceByName(piece), weight);
            }
            CommonClass.WEIGHTED_FOSSIL_BLOCKS_MAP.put(type, blockStates.build());
            for (String period : periods) {
                for (String biome : biomes) {
                    if (!CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.containsKey(period)) {
                        CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.put(period, new HashMap<>());
                    }
                    if (!CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).containsKey(biome)) {
                        CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).put(biome, new SimpleWeightedRandomList.Builder<>());
                    }
                    CommonClass.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).get(biome).add(type, fossil.getWeight().get());
                }
            }
        });
    }
}
