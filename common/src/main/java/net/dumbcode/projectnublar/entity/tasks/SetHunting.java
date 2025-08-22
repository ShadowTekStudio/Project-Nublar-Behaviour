package net.dumbcode.projectnublar.entity.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Predicate;

public class SetHunting<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryTypesInit.IS_HUNGRY.get(), MemoryStatus.VALUE_PRESENT),Pair.of(MemoryTypesInit.IS_DEHYDRATED.get(),MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryTypesInit.HUNTING.get(), MemoryStatus.VALUE_ABSENT));

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    protected Predicate<E> canStartHuntingPredicate = (dinosaur) -> dinosaur instanceof CarnivoreDinosaur && DinoNeedsUtils.isHungry(dinosaur);

    public SetHunting<E> canStartHuntingPredicate(final Predicate<E> predicate){
        this.canStartHuntingPredicate = predicate;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {

        if(entity instanceof CarnivoreDinosaur carnivore && carnivore.hasPack()){
            return false;
        }
        if(BrainUtils.hasMemory(entity, MemoryTypesInit.HUNTING.get())){
            return false;
        }

        return !entity.isChild() && !entity.isHuntingBlocked();
    }

    @Override
    protected void start(E entity) {
        System.out.println("hunting state active");
        BrainUtils.setMemory(entity, MemoryTypesInit.HUNTING.get(), true);
    }
}
