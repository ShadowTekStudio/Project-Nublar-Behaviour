package net.dumbcode.projectnublar.entity.ik.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public interface BoneAccessor {
    /**
     * Do not use in the clientTick only use in the set model positions method
     */
    Vec3 getPosition();

    Vec3 getRotationVec();

    /**
     * @param to     the point to move to
     * @param facing at what the bone should face, if null, the bone will not rotate
     * @param entity the entity the model of the bone belongs to
     */
    void moveTo(Vec3 to, @Nullable Vec3 facing, EntityAccessor entity);

    List<BoneAccessor> getChildren();
}
