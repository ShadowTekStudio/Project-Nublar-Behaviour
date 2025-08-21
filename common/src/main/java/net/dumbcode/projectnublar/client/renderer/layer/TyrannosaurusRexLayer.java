package net.dumbcode.projectnublar.client.renderer.layer;

import net.dumbcode.projectnublar.entity.species.carnivore.TyrannosaurusRexEntity;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class TyrannosaurusRexLayer extends GeoRenderLayer<TyrannosaurusRexEntity> {

    public TyrannosaurusRexLayer(GeoRenderer<TyrannosaurusRexEntity> entityRendererIn) {
        super(entityRendererIn);
    }

}
