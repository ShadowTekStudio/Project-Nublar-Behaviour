package net.dumbcode.projectnublar.entity.tasks;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.Dinosaur;

import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteraction;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;


public class StartTurfWar<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private Random random = new Random();

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(Pair.of(MemoryTypesInit.INITIATED_TURF_WAR.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    protected Predicate<LivingEntity> canAttackPredicate = ( target) -> target.isAlive();
    protected LivingEntity toTarget = null;
    protected MemoryModuleType<? extends LivingEntity> priorityTargetMemory = MemoryModuleType.NEAREST_ATTACKABLE;

    public StartTurfWar<E> attackablePredicate(Predicate<LivingEntity> predicate){
        this.canAttackPredicate = predicate;

        return this;
    }

    public StartTurfWar<E> useMemory(MemoryModuleType<? extends LivingEntity> memory) {
        this.priorityTargetMemory = memory;

        return this;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E carnivore) {
        //Shouldn't initiate if already engaged in social case
        Brain<?> brain = carnivore.getBrain();
        this.toTarget = BrainUtils.getMemory(brain, this.priorityTargetMemory);

        if (this.toTarget == null) {
            this.toTarget = BrainUtils.getMemory(brain, MemoryModuleType.HURT_BY_ENTITY);

            if (this.toTarget == null) {
                NearestVisibleLivingEntities nearbyEntities = BrainUtils.getMemory(brain, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

                if (nearbyEntities != null)
                    this.toTarget = nearbyEntities.findClosest(this.canAttackPredicate).orElse(null);

                if (this.toTarget == null)
                    return false;
            }
        }
        if(BrainUtils.hasMemory(carnivore,MemoryTypesInit.INITIATED_TURF_WAR.get())){
            return false;
        }
        return this.canAttackPredicate.test(this.toTarget) && carnivore.distanceTo(toTarget) < 100.0F;
    }


    @Override
    protected void start(E entity)
    {
        int encounterOutcome = random.nextInt(3);

        //UPDATE BRAIN TO TRIGGER TURF WAR
        BrainUtils.setMemory(toTarget,MemoryTypesInit.INITIATED_TURF_WAR.get(), (byte) 1);
        BrainUtils.setMemory(entity,MemoryTypesInit.INITIATED_TURF_WAR.get(), (byte) 1);
        BrainUtils.setMemory(entity,MemoryTypesInit.TURF_WAR_MEMBER.get(),1);
        BrainUtils.setMemory(toTarget,MemoryTypesInit.TURF_WAR_MEMBER.get(),2);
        BrainUtils.setMemory(entity,MemoryTypesInit.TURF_WAR_OUTCOME.get(),encounterOutcome);
        BrainUtils.setMemory(entity,MemoryTypesInit.SOCIAL_TARGET.get(),(Dinosaur) toTarget);

        //START TURF WAR
        if(entity.getInteractionEntity() == null) {
            entity.spawnInteractionEntity();

            if (entity.getInteractionEntity().getSocialTarget() == null) {
                entity.getInteractionEntity().setSocialTarget((Dinosaur) toTarget);
            }

            if (entity.getInteractionEntity() != null && entity.getInteractionEntity().getSocialTarget() != null) {
                entity.getInteractionEntity().getInteractionManager().setSocialCase(TyrannosaurInteraction.TURF_WAR_TYRANNOSAUR);
                Constants.LOG.debug("Turf war event has begun!");
            }

        } else {
            //Something weird has happened, get rid of the current interactionEntity
            entity.getInteractionEntity().remove(Entity.RemovalReason.DISCARDED);
        }

        //END OF BRAIN TO DO
        this.toTarget = null;
    }
}
