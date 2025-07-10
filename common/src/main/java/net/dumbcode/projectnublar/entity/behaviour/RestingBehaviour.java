package net.dumbcode.projectnublar.entity.behaviour;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;

public class RestingBehaviour<E extends Dinosaur> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryTypesInit.IS_TIRED.get(), MemoryStatus.VALUE_PRESENT));

    protected Predicate<E> canRestPredicate = (dinosaur) -> true;

    public RestingBehaviour<E> canRestPredicate(final Predicate<E> predicate){
        this.canRestPredicate = predicate; return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return (!BrainUtils.hasMemory(entity, MemoryTypesInit.IS_RESTING.get()));
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E dinosaur) {
        System.err.println("trying to rest");
        BrainUtils.setMemory(dinosaur, MemoryTypesInit.IS_RESTING.get(), true);
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.LOOK_TARGET);
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.WALK_TARGET);
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        BrainUtils.clearMemory(dinosaur, MemoryModuleType.ATTACK_TARGET);
        BrainUtils.clearMemory(dinosaur, MemoryTypesInit.HAS_FOUND_WATER.get());

    }
}
