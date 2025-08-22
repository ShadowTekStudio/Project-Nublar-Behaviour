package net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex;

import net.dumbcode.projectnublar.entity.social.interactions.tyrannosaurusrex.DeadTyrannosaur;
import net.dumbcode.projectnublar.entity.social.interactions.tyrannosaurusrex.TyrannosaurusTurfWar;

import java.util.Arrays;
import java.util.function.Function;

public class TyrannosaurInteraction<T extends TyrannosaurInteractionInstance> {

    private static TyrannosaurInteraction<?>[] socialCases = new TyrannosaurInteraction[0];

    public static final TyrannosaurInteraction<TyrannosaurHoldingPattern> HOLDING_PATTERN_TYRANNOSAUR =
            create(TyrannosaurHoldingPattern::new, "holding_pattern_tyrannosaurus_rex");
    public static final TyrannosaurInteraction<TyrannosaurusTurfWar> TURF_WAR_TYRANNOSAUR =
            create(TyrannosaurusTurfWar::new, "turf_war_tyrannosaurus_rex");
   public static final TyrannosaurInteraction<DeadTyrannosaur> DEAD_TYRANNOSAUR =
            create(DeadTyrannosaur::new, "dead_tyrannosaurus_rex");

    private final Function<TyrannosaurInteractionEntity, T> factory;
    private final int id;
    private final String name;

    public TyrannosaurInteraction(int id, Function<TyrannosaurInteractionEntity, T> factory, String name) {
        this.id = id;
        this.factory = factory;
        this.name = name;
    }

    public T createInstance(TyrannosaurInteractionEntity dinosaur) {
        return factory.apply(dinosaur);
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name + "(#" + id + ")";
    }

    public static TyrannosaurInteraction<?> getById(int id) {
        return id >= 0 && id < socialCases.length ? socialCases[id] : HOLDING_PATTERN_TYRANNOSAUR;
    }

    public static int getCount() {
        return socialCases.length;
    }

    private static <T extends TyrannosaurInteractionInstance> TyrannosaurInteraction<T> create(
            Function<TyrannosaurInteractionEntity, T> factory, String name) {
        TyrannosaurInteraction<T> dinosaurSocialCase = new TyrannosaurInteraction<>(socialCases.length, factory, name);
        socialCases = Arrays.copyOf(socialCases, socialCases.length + 1);
        socialCases[dinosaurSocialCase.getId()] = dinosaurSocialCase;
        return dinosaurSocialCase;
    }
}
