package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.ProjectNublar;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.config.FossilsConfig;
import net.dumbcode.projectnublar.init.EntityInit;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    private final static List<String> PERIODS = List.of("carboniferous", "jurassic", "cretaceous");

    @SubscribeEvent
    public static void attribcage(EntityAttributeCreationEvent e) {
        EntityInit.attributeSuppliers.forEach(p -> e.put(p.entityTypeSupplier().get(), p.factory().get().build()));
    }
    @SubscribeEvent
    public static void onConfigLoaded(ModConfigEvent.Loading e) {

        FossilsConfig.getFossils().forEach((type, fossil) -> {
            FossilCollection collection = FossilCollection.COLLECTIONS.get(type);
            List<String> periods = fossil.getPeriods().get();
            List<String> biomes = fossil.getBiomes().get();
            SimpleWeightedRandomList.Builder<FossilPiece> blockStates = new SimpleWeightedRandomList.Builder<>();
            FossilsConfig.Set set = FossilsConfig.getSet(fossil.getPieces().get());
            int setSize = set.pieces.get().size();
            int weightSize = set.weights.get().size();
            try {
                for (int i = 0; i < setSize; i++) {
                    String piece = set.pieces().get().get(i);
                    if( i < weightSize){
                    int weight = set.weights().get().get(i);
                    blockStates.add(FossilPieces.getPieceByName(piece), weight);
                    }
                }
            } catch (IndexOutOfBoundsException ex){
                System.err.println("Index error while building fossil pieces:");
                System.err.println("Type: " + type);
                System.err.println("Pieces size: " + setSize);
                System.err.println("Weights size: " + weightSize);
                ex.printStackTrace();
            }
            ProjectNublar.WEIGHTED_FOSSIL_BLOCKS_MAP.put(type, blockStates.build());
            for (String period : periods) {
                for (String biome : biomes) {
                    if (!ProjectNublar.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.containsKey(period)) {
                        ProjectNublar.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.put(period, new HashMap<>());
                    }
                    if(!ProjectNublar.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).containsKey(biome)){
                        ProjectNublar.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).put(biome, new SimpleWeightedRandomList.Builder<>());
                    }
                    ProjectNublar.WEIGHTED_PERIOD_BIOME_FOSSIL_MAP.get(period).get(biome).add(type, fossil.getWeight().get());
                }
            }
        });
        boolean breakHere = true;
    }
}
