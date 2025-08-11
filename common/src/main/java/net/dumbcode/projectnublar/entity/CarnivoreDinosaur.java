package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.util.BrainUtils;

public class CarnivoreDinosaur extends Dinosaur{
    public CarnivoreDinosaur(EntityType<? extends PathfinderMob> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if(BrainUtils.hasMemory(this, MemoryTypesInit.IS_RESTING.get())){
            return false;
        }
        if(BrainUtils.hasMemory(this, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)){
            return false;
        }
        if(BrainUtils.hasMemory(this, MemoryTypesInit.IS_EATING.get())){
            return false;
        }
        if(BrainUtils.hasMemory(this, MemoryTypesInit.IS_DRINKING.get())){
            return false;
        }
        if(!BrainUtils.hasMemory(this,MemoryTypesInit.IS_HUNGRY.get())){
            return false;
        }

        if(this.entityData.get(HUNGER) > this.getMaxHunger() * this.getDinoBehaviour().lowRisk()){
            return false;
        }

        return target.getVehicle() != this;
    }
}
