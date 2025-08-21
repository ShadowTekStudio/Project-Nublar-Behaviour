package net.dumbcode.projectnublar.entity.species.carnivore;

import net.dumbcode.projectnublar.entity.CarnivoreDinosaur;
import net.dumbcode.projectnublar.init.SoundInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TyrannosaurusRexEntity extends CarnivoreDinosaur {

    public TyrannosaurusRexEntity(EntityType<? extends CarnivoreDinosaur> $$0, Level $$1) {
        super($$0, $$1);
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
