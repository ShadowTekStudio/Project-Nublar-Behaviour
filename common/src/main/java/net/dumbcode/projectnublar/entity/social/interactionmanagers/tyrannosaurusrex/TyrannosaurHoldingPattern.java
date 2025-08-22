package net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex;

public class TyrannosaurHoldingPattern  extends AbstractTyrannosaurSocialCaseInstance{

    public TyrannosaurHoldingPattern(TyrannosaurInteractionEntity interactionEntity) {
        super(interactionEntity);
    }

    @Override
    public TyrannosaurInteraction<TyrannosaurHoldingPattern> getSocialCase() {
        return TyrannosaurInteraction.HOLDING_PATTERN_TYRANNOSAUR;
    }

}

