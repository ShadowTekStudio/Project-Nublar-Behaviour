package net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex;

import net.dumbcode.projectnublar.entity.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.PackEntity;
import net.dumbcode.projectnublar.entity.species.carnivore.TyrannosaurusRexEntity;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class AbstractTyrannosaurSocialCaseInstance implements TyrannosaurInteractionInstance {
    protected final TyrannosaurInteractionEntity interactionEntity;

    public final TyrannosaurusRexEntity rex;
    private @Nullable Dinosaur socialTarget;

    public AbstractTyrannosaurSocialCaseInstance(TyrannosaurInteractionEntity interactionEntity){
        this.interactionEntity = interactionEntity;

        this.rex = interactionEntity.getHost();
    }

    public TyrannosaurusRexEntity getHost(){
        return this.rex;
    }
    public @Nullable Dinosaur getTarget(){
        return this.socialTarget;
    }
    public void setSocialTarget(Dinosaur target){
        this.socialTarget = target;
    }

    public void doClientTick() {}

    public void doServerTick() {
    }

    public void begin() {}
    public void end() {}

    public @Nullable PackEntity getPackEntity(Dinosaur dinosaur) {
        if(dinosaur instanceof CarnivoreDinosaur carnivoreDinosaur && carnivoreDinosaur.hasPack()){
            return carnivoreDinosaur.getPackEntity();
        } else {
            return null;
        }
    }

    public @Nullable Dinosaur getDinosaurMate(Dinosaur dinosaur) {
        @Nullable Dinosaur mate = null;

        if(dinosaur.hasMate()){
            if(dinosaur.getEntityData().get(Dinosaur.DINO_MATE).isPresent()) {
               mate = (Dinosaur) dinosaur.getServer().getLevel(dinosaur.level().dimension()).getEntity(dinosaur.getEntityData().get(Dinosaur.DINO_MATE).get());
            }
        }
        return mate;
    }

    public @Nullable List<Dinosaur> getDinosaurOffspring(Dinosaur dinosaur) {
       if(BrainUtils.hasMemory(dinosaur,MemoryTypesInit.OFFSPRING_LIST.get())) {
           return BrainUtils.getMemory(dinosaur, MemoryTypesInit.OFFSPRING_LIST.get());
       } else return null;
    }

    public float getThreatLevel(LivingEntity entity) {
        return DinoNeedsUtils.getThreatScore(entity);
    }

    public double getHuntScore(LivingEntity entity) {
        return DinoNeedsUtils.getHuntTargetValue(entity);
    }

    public float onHurt(DamageSource source, float damageAmount) {
        return damageAmount;
    }

    /// TODO:ADD NESTS
   public double distanceFromNest(Dinosaur socialTarget) {
        return 0;
   }

    public double distanceFromMate(Dinosaur dinosaur) {
       @Nullable Dinosaur mate = this.getDinosaurMate(dinosaur);
       if(mate != null){
           return dinosaur.distanceTo(mate);
       } else {
           return 0.00D;
       }
    }

    public double distanceFromClosestOffspring(Dinosaur dinosaur) {
        @Nullable List<Dinosaur> targetOffspring = this.getDinosaurOffspring(dinosaur);
        double closestDistance = 0.0;
        if(targetOffspring != null) {

            closestDistance = targetOffspring.get(0).distanceTo(dinosaur);

            for (Dinosaur offspring : targetOffspring){
                double distance = dinosaur.distanceTo(offspring);

                if (distance < closestDistance){
                    closestDistance = distance;
                }

            }

        }
        return closestDistance;
    }
}
