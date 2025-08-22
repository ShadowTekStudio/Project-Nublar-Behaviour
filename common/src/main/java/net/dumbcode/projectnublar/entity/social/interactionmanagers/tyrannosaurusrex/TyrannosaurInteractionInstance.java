package net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.PackEntity;

import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface TyrannosaurInteractionInstance {

    void doClientTick();
    void doServerTick();

    @Nullable PackEntity getPackEntity(Dinosaur dinosaur);
    @Nullable Dinosaur getDinosaurMate(Dinosaur dinosaur);
    @Nullable List<Dinosaur> getDinosaurOffspring(Dinosaur dinosaur);

    TyrannosaurInteraction<? extends TyrannosaurInteractionInstance> getSocialCase();

    float onHurt(DamageSource var1, float var2);

    void begin();
    void end();

    double distanceFromNest(Dinosaur socialTarget);
    double distanceFromMate(Dinosaur socialTarget);
    double distanceFromClosestOffspring(Dinosaur socialTarget);

}
