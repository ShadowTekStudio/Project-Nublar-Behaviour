package net.dumbcode.projectnublar.entity.behaviour;

import net.dumbcode.projectnublar.api.DinoDietData;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DinosaurEatGoal extends Goal {
    private final Dinosaur dinosaur;
    private double foodIncrement;
    private @Nullable ItemStack food;
    private DinoDietData dietData;

    public DinosaurEatGoal(Dinosaur pDinosaur, String dietType){
        this.dinosaur = pDinosaur;
    }
    @Override
    public boolean canUse() {
    this.dietData = this.foodData();
        return this.dinosaur.isHungry() && this.dietData != null;
    }
    public void checkValidItemAndFeedDinosaur(){
        List<ItemEntity> nearbyDrops = this.dinosaur.level().getEntitiesOfClass(
                ItemEntity.class, dinosaur.getBoundingBox().inflate(4.0),
                item -> !item.hasPickUpDelay() && item.isAlive()
        );
        if(!nearbyDrops.isEmpty()) {
                for (ItemEntity itemEntity : nearbyDrops) {
                    String foodID = itemEntity.getItem().getDescriptionId();
                    if (this.foodData() != null) {
                        if (this.foodData().foodMap().containsKey(foodID)) {
                            this.food = itemEntity.getItem();
                            if(!this.food.is(Items.AIR)) { //without this for some reason it might eat air if eating too many food items at once
                                this.getFoodIncrement();
                                this.food.shrink(1);
                                this.dinosaur.feed(this.foodIncrement);
                                this.food = null;
                            }
                        }
                    }
                }
        }
    }
    public double getFoodIncrement(){
        if(this.dietData != null) {
            for (Map.Entry<String, Double> itemID : this.foodData().foodMap().entrySet()) {
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

    public DinoDietData foodData(){return this.dietData = DietReloadListener.getDietInfoForType(this.dinosaur.getDietType());}

    @Override public void tick() {
        if(this.canUse()){
            this.checkValidItemAndFeedDinosaur();
        }
    }

}
