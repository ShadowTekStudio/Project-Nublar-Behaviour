package net.dumbcode.projectnublar.entity.behaviour;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;


public class DrinkBehaviour<E extends Dinosaur> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryTypesInit.HAS_FOUND_WATER.get(), MemoryStatus.VALUE_PRESENT),Pair.of(MemoryTypesInit.IS_THIRSTY.get(), MemoryStatus.VALUE_PRESENT));

    protected Predicate<? extends BlockState> targetPredicate = (blockState) -> true;
    protected Predicate<E> canTargetPredicate = (dinosaur) -> true;


    public DrinkBehaviour<E> targetPredicate(final Predicate<BlockState> predicate) {
        this.targetPredicate = predicate; return this;
    }
    public DrinkBehaviour<E> canTargetPredicate(final Predicate<E> predicate) {
        this.canTargetPredicate = predicate; return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }


    @Override
    protected void start(E dinosaur) {
        BlockPos nearestWaterSource = BrainUtils.getMemory(dinosaur,MemoryTypesInit.HAS_FOUND_WATER.get());

        if (dinosaur.distanceToSqr(nearestWaterSource.getCenter()) <= 5) {
            BrainUtils.setMemory(dinosaur, MemoryTypesInit.IS_DRINKING.get(), true);
        }
    }

}
