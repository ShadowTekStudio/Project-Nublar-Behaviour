package net.dumbcode.projectnublar.entity.behaviour;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class EatBehaviour<E extends Dinosaur> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT));

    //Not working yet, need to figure out how to adapt for new approach

    protected BiPredicate<E,? extends ItemEntity> targetPredicate = (dinosaur, foodItem) -> true ;
    private DinoDietData dietData;
    private double foodIncrement;
    private @Nullable ItemStack food;
    private int animTickCounter;

    public EatBehaviour(int delayTicks) {
        super(delayTicks);
    }


    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }



    @Override
    protected void start(E dinosaur) {
       ItemEntity nearbyFood;

       if(BrainUtils.hasMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)){
           nearbyFood = BrainUtils.getMemory(dinosaur, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
               if(nearbyFood != null && this.foodData(dinosaur).foodMap().containsKey(nearbyFood.getItem().getDescriptionId())){
                   if(dinosaur.distanceToSqr(nearbyFood) < 5){
                       this.food = nearbyFood.getItem();
                       dinosaur.feed((float) this.foodIncrement);
                       this.food.shrink(1);
                       this.getFoodIncrement(dinosaur);
                   }
               }
           }

    }

    public DinoDietData foodData(E dinosaur){
        if(this.dietData == null) {
            return this.dietData = DietReloadListener.getDietInfoForType(dinosaur.getEntityData().get(Dinosaur.DINO_BEHAVIOUR).getString("diet_id"));
        }
        return this.dietData;
    }
    public double getFoodIncrement(E dinosaur){
        if(this.dietData != null) {
            for (Map.Entry<String, Double> itemID : this.foodData(dinosaur).foodMap().entrySet()) {
                if(this.food != null){
                    if (itemID.getKey().equals(this.food.getDescriptionId())) {
                        this.foodIncrement = itemID.getValue();
                    }
                }
            }
        }
        if(this.foodIncrement == 0){this.foodIncrement = 1;}
        return this.foodIncrement;
    }
    public EatBehaviour<E> targetPredicate(BiPredicate<E,ItemEntity> predicate) {
        this.targetPredicate = predicate;

        return this;
    }

    @Override
    protected void stop(E entity) {
        entity.getBrain().eraseMemory(MemoryTypesInit.IS_EATING.get());
        entity.getBrain().eraseMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        this.food = null;
    }

}
