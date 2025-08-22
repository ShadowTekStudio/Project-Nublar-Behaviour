package net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.species.carnivore.TyrannosaurusRexEntity;
import net.dumbcode.projectnublar.init.MemoryTypesInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class TyrannosaurInteractionEntity extends Entity {

    public static final EntityDataAccessor<Boolean> INTERACTION_FINISHED = SynchedEntityData.defineId(TyrannosaurInteractionEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> DATA_SOCIAL_CASE = SynchedEntityData.defineId(TyrannosaurInteractionEntity.class, EntityDataSerializers.INT);

    private TyrannosaurusRexEntity host;
    private @Nullable Dinosaur socialTarget;
    private final TyrannosaurSocialManager tyrannosaurSocialManager;

    public TyrannosaurInteractionEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.tyrannosaurSocialManager = new TyrannosaurSocialManager(this);
    }

    public TyrannosaurInteractionEntity(EntityType<?> entityType, Level level, TyrannosaurusRexEntity host) {
        super(entityType, level);
        this.host = host;
        this.socialTarget = null;
        this.setPos(host.position());
        this.tyrannosaurSocialManager = new TyrannosaurSocialManager(this);
        this.noPhysics = true;

    }
    public TyrannosaurInteractionEntity(EntityType<?> entityType, Level level, TyrannosaurusRexEntity host, @Nullable Dinosaur socialTarget) {
        super(entityType, level);
        this.host = host;
        this.socialTarget = socialTarget;
        this.setPos(host.position());
        this.tyrannosaurSocialManager = new TyrannosaurSocialManager(this);
        this.noPhysics = true;

    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(INTERACTION_FINISHED, false);
        this.entityData.define(DATA_SOCIAL_CASE, TyrannosaurInteraction.HOLDING_PATTERN_TYRANNOSAUR.getId());

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pTag) {
        if(pTag.contains("dino_social_case")){
            this.tyrannosaurSocialManager.setSocialCase(TyrannosaurInteraction.getById(pTag.getInt("dino_social_case")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("dino_social_case", this.tyrannosaurSocialManager.getCurrentSocialCase().getSocialCase().getId());
    }

    public TyrannosaurSocialManager getInteractionManager(){
        return this.tyrannosaurSocialManager;
    }

    public TyrannosaurusRexEntity getHost() {
        return this.host;
    }
    public @Nullable Dinosaur getSocialTarget() {

        if(this.socialTarget == null && getHost() != null) {
            if (BrainUtils.hasMemory(getHost(), MemoryTypesInit.SOCIAL_TARGET.get())) {
                @Nullable Dinosaur targetFromMemory = BrainUtils.getMemory(host, MemoryTypesInit.SOCIAL_TARGET.get());
                this.setSocialTarget(targetFromMemory);
            }
        }
        return this.socialTarget;
    }

    public void setSocialTarget(Dinosaur dinosaur) {
        this.socialTarget = dinosaur;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        this.getHost().clearInteractionEntity();

        if(this.getSocialTarget() != null) {
            this.getSocialTarget().clearInteractionEntity();
        }

        super.remove(reason);
    }

    @Override
    public void tick() {
        super.tick();


            if (this.entityData.get(INTERACTION_FINISHED)) {
                this.remove(RemovalReason.DISCARDED);
            }

            if (this.level().isClientSide) {
                this.tyrannosaurSocialManager.getCurrentSocialCase().doClientTick();
            } else {
                TyrannosaurInteractionInstance currentSocialCase = this.tyrannosaurSocialManager.getCurrentSocialCase();
                currentSocialCase.doServerTick();
                if (this.tyrannosaurSocialManager.getCurrentSocialCase() != currentSocialCase) {
                    currentSocialCase = this.tyrannosaurSocialManager.getCurrentSocialCase();
                    currentSocialCase.doServerTick();
                }
            }

    }
}
