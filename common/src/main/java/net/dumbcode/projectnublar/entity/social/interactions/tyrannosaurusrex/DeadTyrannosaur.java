package net.dumbcode.projectnublar.entity.social.interactions.tyrannosaurusrex;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.AbstractTyrannosaurSocialCaseInstance;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteraction;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionEntity;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionInstance;

public class DeadTyrannosaur extends AbstractTyrannosaurSocialCaseInstance {
    public DeadTyrannosaur(TyrannosaurInteractionEntity dinosaur) {
        super(dinosaur);
    }

    @Override
    public TyrannosaurInteraction<? extends TyrannosaurInteractionInstance> getSocialCase() {
        return TyrannosaurInteraction.DEAD_TYRANNOSAUR;
    }
}
