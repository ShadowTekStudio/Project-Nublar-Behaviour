package net.dumbcode.projectnublar.entity.behaviour.carnivore;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.dumbcode.projectnublar.init.SoundInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class RoarAtThreat<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));

    public RoarAtThreat(int delayTicks) {
        super(delayTicks);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        List<LivingEntity> nearbyEntities = BrainUtils.getMemory(entity,MemoryModuleType.NEAREST_LIVING_ENTITIES);
        boolean canRoar = false;

        if(!nearbyEntities.isEmpty()) {
            for (LivingEntity target : nearbyEntities) {
                if (!entity.isFamily(target) && target.distanceToSqr(entity) < 10) {
                    canRoar = true;
                }
            }
        }

        return canRoar && !entity.isRoaring();
    }

    @Override
    protected void start(E entity) {
        BrainUtils.setMemory(entity, MemoryTypesInit.IS_ROARING.get(), true);
        entity.playSound(SoundInit.TYRANNOSAUR_ROAR.get(), 10,1);
        entity.playSound(SoundEvents.GENERIC_EAT);
    }

    @Override
    protected void doDelayedAction(E entity) {
        BrainUtils.clearMemory(entity,MemoryTypesInit.IS_ROARING.get());

    }

    @Override
    protected void stop(E entity) {
        BrainUtils.clearMemory(entity,MemoryTypesInit.IS_ROARING.get());
    }
}
