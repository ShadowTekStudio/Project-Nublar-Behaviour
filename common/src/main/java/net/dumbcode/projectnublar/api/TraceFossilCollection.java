package net.dumbcode.projectnublar.api;

import dev.architectury.registry.registries.DeferredSupplier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public record TraceFossilCollection(Map<Block, DeferredSupplier<Block>> stoneMap) {
    public static Map<Block, DeferredSupplier<Block>> TRACE_FOSSILS;

    public static TraceFossilCollection register(String traceName){
        Map<Block,DeferredSupplier<Block>> map = new HashMap<>();
        return new TraceFossilCollection(map);
    }

}
