package net.dumbcode.projectnublar.init;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class MemoryTypesInit {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES = DeferredRegister.create(Constants.MODID, Registries.MEMORY_MODULE_TYPE);

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_DRINKING =
            MEMORIES.register("is_dino_drinking", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_HUNGRY =
            MEMORIES.register("is_dino_hungry", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_STARVING =
            MEMORIES.register("is_dino_starving", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_DEHYDRATED =
            MEMORIES.register("is_dino_dehydrated", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_EXHAUSTED =
            MEMORIES.register("is_dino_exhausted", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_THIRSTY =
            MEMORIES.register("is_dino_thirsty", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_TIRED =
            MEMORIES.register("is_dino_tired", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_RESTING =
            MEMORIES.register("is_dino_resting", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_SITTING =
            MEMORIES.register("is_dino_sitting", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> GETTING_UP =
            MEMORIES.register("is_dino_rising", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_SLEEPING =
            MEMORIES.register("is_dino_sleeping", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<BlockPos>> HAS_FOUND_WATER =
            MEMORIES.register( "found_water_source", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<ItemEntity>> FOUND_FOOD_ITEM =
            MEMORIES.register( "found_food_item", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> HUNTING =
            MEMORIES.register("is_dino_hunting", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_EATING =
            MEMORIES.register("is_dino_eating", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_ROARING =
            MEMORIES.register("is_dino_roaring", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<UUID>> MATE_UUID  =
            MEMORIES.register("dino_mate_id", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Dinosaur>> MATE  =
            MEMORIES.register("dino_mate", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<List<Dinosaur>>> NEAREST_DINOSAURS  =
            MEMORIES.register("dino_nearby_peers", ()-> new MemoryModuleType<>(Optional.empty()));


    public static void loadClass(){MEMORIES.register();}


}
