package net.dumbcode.projectnublar.entity.ik.model;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface EntityAccessor {
    Vec3 getPosition();
    
    Vec3 getOldPosition();
    
    Level getLevel();
    
    double getYRot();

    Vec3 getForwardFacingVector();
}
