package net.dumbcode.projectnublar.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public class HerbivoreDinosaur extends Dinosaur{

    public HerbivoreDinosaur(EntityType<? extends Dinosaur> $$0, Level $$1) {
        super($$0, $$1, 39);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        super.canTarget(target);
        if(this.getLastAttacker() != target ){
            return false;
        }
        return target.getVehicle() != this;
    }
}
