package net.dumbcode.projectnublar.entity.ik.model.GeckoLib;

import net.dumbcode.projectnublar.entity.ik.model.BoneAccessor;
import net.dumbcode.projectnublar.entity.ik.model.ModelAccessor;
import net.dumbcode.projectnublar.entity.ik.util.ChainCommonClass;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public class GeoModelAccessor implements ModelAccessor {
    private final GeoModel<? extends GeoAnimatable> model;

    public GeoModelAccessor(GeoModel<? extends GeoAnimatable> model) {
        this.model = model;
    }

    @Override
    public Optional<BoneAccessor> getBone(String boneName) {
        Optional<GeoBone> optionalGeoBone = this.model.getBone(boneName);

        if (optionalGeoBone.isEmpty()) {
            ChainCommonClass.throwInDevOnly(new BoneNotFoundException(boneName));
            return Optional.empty();
        }

        GeoBone bone = optionalGeoBone.get();

        return  Optional.of((BoneAccessor) bone);
    }

    public class BoneNotFoundException extends RuntimeException {
        public BoneNotFoundException(String boneName) {
            super("Bone not found: " + boneName);
        }
    }
}
