package net.dumbcode.projectnublar.entity.species.carnivore;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteraction;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionEntity;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionInstance;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurSocialManager;
import net.dumbcode.projectnublar.entity.species.DinosaurPart;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.dumbcode.projectnublar.init.SoundInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TyrannosaurusRexEntity extends CarnivoreDinosaur {

    private  DinosaurPart left_leg;
    private  DinosaurPart right_leg;
    private  DinosaurPart body;
    private  DinosaurPart tail1;
    private  DinosaurPart tail2;
    private  DinosaurPart tail3;


    public TyrannosaurusRexEntity(EntityType<? extends TyrannosaurusRexEntity> $$0, Level $$1) {
        super($$0, $$1, 34);
       resetParts(2.0F);
       updateParts();
    }

    private @Nullable TyrannosaurInteractionEntity interactionEntity;

    public void resetParts(float scale) {
        removeParts();
        head = new DinosaurPart(this,"head", 1.0F * scale, 0, 2.0F * scale, 1F * scale, 1F * scale, 1.5F);
        head.setParent(this);

        this.subEntities = new DinosaurPart[]{this.head};

    }

    @Override
    public void spawnInteractionEntity() {
        EntityType<?> type = EntityInit.INTERACTION_ENTITY.get();
        TyrannosaurInteractionEntity newInteractionEntity;

        if(BrainUtils.hasMemory(this, MemoryTypesInit.SOCIAL_TARGET.get())) {
            Dinosaur socialTarget = BrainUtils.getMemory(this, MemoryTypesInit.SOCIAL_TARGET.get());
             newInteractionEntity = new TyrannosaurInteractionEntity(type, this.level(), this, socialTarget);
                this.level().addFreshEntity(newInteractionEntity);
                newInteractionEntity.setPos(this.position());
                setInteractionEntity(newInteractionEntity);
                socialTarget.setInteractionEntity(newInteractionEntity);
        } else {
             newInteractionEntity = new TyrannosaurInteractionEntity(type, this.level(), this);
            this.level().addFreshEntity(newInteractionEntity);
            newInteractionEntity.setPos(this.position());
            setInteractionEntity(newInteractionEntity);
        }
    }

    @Override
    public void clearInteractionEntity() {
        this.interactionEntity = null;
    }

    @Override
    public void setInteractionEntity(TyrannosaurInteractionEntity pInteractionEntity){
        this.interactionEntity = pInteractionEntity;
    }

    @Override
    public @Nullable TyrannosaurInteractionEntity getInteractionEntity(){
        return this.interactionEntity;
    }

    public void removeParts() {
        if (head != null) {
            head.remove(RemovalReason.DISCARDED);
            head = null;
        }
        if (subEntities != null) {
            subEntities = null;
        }
    }
    public void updateParts(){
        updatePart(head, this);
        this.subEntities = new DinosaurPart[]{this.head};
    }

    public static void updatePart(@Nullable final DinosaurPart part, @NotNull final Dinosaur parent) {
        if (part == null || !(parent.level() instanceof ServerLevel serverLevel) || parent.isRemoved()) {
            return;
        }

        if (!part.shouldContinuePersisting()) {
            UUID uuid = part.getUUID();
            Entity existing = serverLevel.getEntity(uuid);

            // Update UUID if a different entity with the same UUID exists already
            if (existing != null && existing != part) {
                while (serverLevel.getEntity(uuid) != null) {
                    uuid = Mth.createInsecureUUID(parent.getRandom());
                }
                Constants.LOG.debug("Updated the UUID of [{}] due to a clash with [{}]", part, existing);
            }

            part.setUUID(uuid);
            serverLevel.addFreshEntity(part);
        }
        part.setParent(parent);
        if (parent != null && !part.level().isClientSide) {
            float renderYawOffset =  parent.yBodyRot;

            if(part.getPartName().equals("head")) {
                part.setPos(parent.getHeadBonePos());
                //   part.setPos(parent.getHeadBonePos().x + part.radius * Mth.cos((float) (renderYawOffset * (Math.PI / 180.0F) + part.angleYaw)), parent.getEntityData().get(Dinosaur.DINOSAUR_HEAD_Y), parent.getHeadBonePos().z + part.radius * Mth.sin((float) (renderYawOffset * (Math.PI / 180.0F) + part.angleYaw)));
            }


        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

    }

    @Override
    public void readAdditionalSaveData(CompoundTag pTag) {
        super.readAdditionalSaveData(pTag);



    }

    @Override
    public void tick() {
        super.tick();
        refreshDimensions();
        updateParts();
    }


    @Override
    public void remove(RemovalReason reason) {
        removeParts();
        super.remove(reason);
    }

    @Override
    public @Nullable SoundEvent getRoarSound() {return SoundInit.TYRANNOSAUR_ROAR.get();}
    @Override
    protected @Nullable SoundEvent getAmbientSound() {return SoundInit.TYRANNOSAUR_BREATH.get();}
    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundInit.TYRANNOSAUR_HURT.get();
    }
    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundInit.TYRANNOSAUR_DEATH.get();
    }
    @Override
    public @Nullable SoundEvent getAttackGrowlSound() {return SoundInit.TYRANNOSAUR_GROWL.get();}
    @Override
    public @Nullable SoundEvent getAttackSound() {return SoundInit.TYRANNOSAUR_BITE.get();}
}
