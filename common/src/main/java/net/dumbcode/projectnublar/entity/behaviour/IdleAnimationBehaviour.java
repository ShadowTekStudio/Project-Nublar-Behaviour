package net.dumbcode.projectnublar.entity.behaviour;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.Util;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import software.bernie.geckolib.animatable.GeoEntity;

public class  IdleAnimationBehaviour<E extends LivingEntity & GeoEntity> extends Idle<E> {


    @Override
    protected void start(E entity) {
        super.start(entity);
        entity.triggerAnim(Dinosaur.MAIN_CONTROLLER, Util.getRandom(Dinosaur.idleAnimations,entity.getRandom()));
    }
}
