package net.dumbcode.projectnublar.entity.ik.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityEntityAccessor implements EntityAccessor {
    private Entity entity;

    public EntityEntityAccessor(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Vec3 getPosition() {
        return this.entity.position();
    }

    @Override
    public Vec3 getOldPosition() {
        return new Vec3(this.entity.xOld, this.entity.yOld, this.entity.zOld);
    }

    @Override
    public Level getLevel() {
        return this.entity.level();
    }

    @Override
    public double getYRot() {
        return this.entity.getYRot();
    }

    @Override
    public Vec3 getForwardFacingVector() {
        return this.entity.getForward();
    }
}
