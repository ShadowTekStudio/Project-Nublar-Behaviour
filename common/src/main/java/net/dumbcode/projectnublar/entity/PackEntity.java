package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.entity.api.IPackBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class PackEntity extends Entity {

    public static final EntityDataAccessor<CompoundTag> DINO_PACK_MEMBERS = SynchedEntityData.defineId(PackEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Optional<UUID>> PACK_LEADER = SynchedEntityData.defineId(PackEntity.class, EntityDataSerializers.OPTIONAL_UUID);


    public PackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        System.out.println("Pack has been spawned");
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DINO_PACK_MEMBERS, new CompoundTag());
        this.entityData.define(PACK_LEADER, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pTag) {
        this.entityData.set(DINO_PACK_MEMBERS, pTag.getCompound("dino_pack_members"));

        if(pTag.contains("dino_pack_leader")){
            this.entityData.set(PACK_LEADER, Optional.of(pTag.getUUID("dino_pack_leader")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("dino_pack_members", this.entityData.get(DINO_PACK_MEMBERS));

        if(this.entityData.get(PACK_LEADER).isPresent()){
            tag.putUUID("dino_pack_leader", this.entityData.get(PACK_LEADER).get());
        }
    }

    public Optional<UUID> getPackLeader() {
        return this.entityData.get(PACK_LEADER);
    }

    public void registerWithPack(Dinosaur dinosaur) {
        CompoundTag pack = this.getEntityData().get(DINO_PACK_MEMBERS);
        pack.putUUID(dinosaur.getUUID().toString(),dinosaur.getUUID());
        this.getEntityData().set(DINO_PACK_MEMBERS, pack);
    }

    public void setPackLeader(Optional<UUID> leaderID) {
        this.entityData.set(PACK_LEADER, leaderID);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }
}
