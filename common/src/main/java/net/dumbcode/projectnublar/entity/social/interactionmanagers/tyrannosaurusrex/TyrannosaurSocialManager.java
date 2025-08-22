package net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex;

import com.mojang.logging.LogUtils;
import net.dumbcode.projectnublar.entity.Dinosaur;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;


public class TyrannosaurSocialManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final TyrannosaurInteractionEntity dinosaur;
    private final TyrannosaurInteractionInstance[] socialCases = new TyrannosaurInteractionInstance[TyrannosaurInteraction.getCount()];
    @Nullable
    private TyrannosaurInteractionInstance currentSocialCase;

    public TyrannosaurSocialManager(TyrannosaurInteractionEntity dinosaur){
        this.dinosaur = dinosaur;
        this.setSocialCase(TyrannosaurInteraction.HOLDING_PATTERN_TYRANNOSAUR);
    }

    public void setSocialCase(TyrannosaurInteraction<?> socialCase){
        if(this.currentSocialCase == null || socialCase != this.currentSocialCase.getSocialCase()){
            if(this.currentSocialCase != null){
                this.currentSocialCase.end();
            }

            this.currentSocialCase = this.getSocialCase(socialCase);
            if(!this.dinosaur.level().isClientSide){
                this.dinosaur.getEntityData().set(TyrannosaurInteractionEntity.DATA_SOCIAL_CASE, socialCase.getId());
            }

            LOGGER.debug("Dinosaur is now in social case {} on the {}",socialCase,this.dinosaur.level().isClientSide ? "client" : "server");
            this.currentSocialCase.begin();
        }
    }
    public TyrannosaurInteractionInstance getCurrentSocialCase(){return this.currentSocialCase;}

    public<T extends TyrannosaurInteractionInstance> T getSocialCase(TyrannosaurInteraction<T> socialCase){
        int i = socialCase.getId();
        if(this.socialCases[i] == null){
            this.socialCases[i] = socialCase.createInstance(this.dinosaur);
        }

        return (T) this.socialCases[i];
    }



}
