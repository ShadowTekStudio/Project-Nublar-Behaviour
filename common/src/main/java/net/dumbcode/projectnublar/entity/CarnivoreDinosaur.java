package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.entity.behaviour.carnivore.RoarAtThreat;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.dumbcode.projectnublar.init.SoundInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class CarnivoreDinosaur extends Dinosaur{

    public static EntityDataAccessor<CompoundTag> DINO_PACK = SynchedEntityData.defineId(CarnivoreDinosaur.class, EntityDataSerializers.COMPOUND_TAG);
    public static EntityDataAccessor<Optional<UUID>> PACK_LEADER = SynchedEntityData.defineId(CarnivoreDinosaur.class, EntityDataSerializers.OPTIONAL_UUID);


    public CarnivoreDinosaur(EntityType<? extends Dinosaur> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        super.canTarget(target);

        if(this.getLastAttacker() == target ){
            return true;
        }

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

        if(!BrainUtils.hasMemory(this,MemoryTypesInit.IS_HUNGRY.get()) ){
            return false;
        }

        return target.getVehicle() != this;
    }

}
