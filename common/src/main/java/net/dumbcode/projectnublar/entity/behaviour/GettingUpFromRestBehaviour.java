package net.dumbcode.projectnublar.entity.behaviour;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class GettingUpFromRestBehaviour <E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryTypesInit.GETTING_UP.get(), MemoryStatus.VALUE_PRESENT));

    public GettingUpFromRestBehaviour(int delayTicks) {
        super(delayTicks);
    }


    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void doDelayedAction(E entity) {
        super.doDelayedAction(entity);
        BrainUtils.clearMemory(entity, MemoryTypesInit.GETTING_UP.get());
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity, MemoryTypesInit.GETTING_UP.get());
    }
}