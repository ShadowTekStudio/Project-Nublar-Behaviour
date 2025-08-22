package net.dumbcode.projectnublar.entity.social.interactions.tyrannosaurusrex;

import net.dumbcode.projectnublar.entity.Dinosaur;

import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.AbstractTyrannosaurSocialCaseInstance;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteraction;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionEntity;
import net.dumbcode.projectnublar.entity.species.carnivore.TyrannosaurusRexEntity;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

public class TyrannosaurusTurfWar extends AbstractTyrannosaurSocialCaseInstance {

    private int turfWarTicks;

    public TyrannosaurusTurfWar(TyrannosaurInteractionEntity interactionEntity) {
        super(interactionEntity);
    }

    @Override
    public void begin() {
        super.begin();

        BrainUtils.clearMemory(getHost(),MemoryModuleType.WALK_TARGET);
        BrainUtils.clearMemory(getHost(),MemoryModuleType.ATTACK_TARGET);
        this.setSocialTarget(this.interactionEntity.getSocialTarget());

    }

    @Override
    public void end() {
            BrainUtils.clearMemory(getHost(), MemoryTypesInit.INITIATED_TURF_WAR.get());
            BrainUtils.clearMemory(getHost(), MemoryTypesInit.TURF_WAR_MEMBER.get());
            if(getTarget() != null) {

                BrainUtils.clearMemory(getTarget(), MemoryTypesInit.INITIATED_TURF_WAR.get());
                BrainUtils.clearMemory(getTarget(), MemoryTypesInit.TURF_WAR_MEMBER.get());
            }
            turfWarTicks = 0;
            this.interactionEntity.getEntityData().set(TyrannosaurInteractionEntity.INTERACTION_FINISHED, true);

    }


    public int getTurfWarIdNo(){
        if(BrainUtils.hasMemory(getHost(),MemoryTypesInit.TURF_WAR_MEMBER.get())) {
            return BrainUtils.getMemory(getHost(), MemoryTypesInit.TURF_WAR_MEMBER.get());
        } else return 1;
    }


    @Override
    public void doServerTick() {
        super.doServerTick();

        if(this.getTarget() == null){
            this.end();
            return;
        }

        if(getHost().isDeadOrDying() || getTarget().isDeadOrDying()){
            this.end();
            return;
        }

        if (turfWarTicks != 0) {
            turfWarTicks++;
        }

        double stopDistance = 10.0F;

        if(getTarget() != null) {
            TyrannosaurusRexEntity host = getHost();
            Dinosaur target = getTarget();

            if (host.distanceTo(target) > 10 && turfWarTicks == 0) {
                BrainUtils.setMemory(host, MemoryModuleType.WALK_TARGET, new WalkTarget(target.position(), 1.0F, 10));
            }

            if (host.distanceTo(target) < 12) {
                if (turfWarTicks == 0) {
                    turfWarTicks++;
                }
            }

            //Maybe Roar at beginning
            if (turfWarTicks == 40) {
                if (getTurfWarIdNo() == 1) {
                    BrainUtils.setMemory(host, MemoryTypesInit.IS_ROARING.get(), true);
                }

            }
            if (turfWarTicks == 80) {
                if (getTurfWarIdNo() == 2) {
                    BrainUtils.setMemory(host, MemoryTypesInit.IS_ROARING.get(), true);
                }
            }


            if (turfWarTicks == 100) {

                if (BrainUtils.hasMemory(host, MemoryTypesInit.TURF_WAR_OUTCOME.get())) {
                    int outcome = BrainUtils.getMemory(host, MemoryTypesInit.TURF_WAR_OUTCOME.get());

                    if (outcome == 1) {
                        doFight();
                    }
                    if (outcome == 2) {
                        doThreatDisplayTargetFlees();
                    }
                    if (outcome == 3){
                        end();
                    }
                }
            }
            if (target == null || host.isDeadOrDying() || target.isDeadOrDying()) {
                this.end();
            }
        }
    }

    public void doFight(){
        BrainUtils.clearMemory(getHost(), MemoryModuleType.WALK_TARGET);
        BrainUtils.setTargetOfEntity(getHost(), getTarget());
        BrainUtils.clearMemory(getHost(), MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        end();
    }
    public void doThreatDisplayTargetFlees() {
        BrainUtils.setMemory(getHost(), MemoryTypesInit.IS_ROARING.get(), true);
        BrainUtils.setMemory(getTarget(), MemoryModuleType.IS_PANICKING, true);
        end();
    }

    @Override
    public TyrannosaurInteraction<TyrannosaurusTurfWar> getSocialCase() {
        return TyrannosaurInteraction.TURF_WAR_TYRANNOSAUR;
    }

}
