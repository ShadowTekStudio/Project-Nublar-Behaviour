package net.dumbcode.projectnublar.entity.api;

import net.minecraft.world.damagesource.DamageSource;

public interface IDinoPart {
    boolean hurt(DamageSource source, float amount);
    double getX();
    double getY();
    double getZ();
    void setPos(double x, double y, double z);
}
