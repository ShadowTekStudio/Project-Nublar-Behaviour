package net.dumbcode.projectnublar.client.renderer.layer;

import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class DinoLayer {
    private final String layerName;
    private final int basicLayer;
    private final Function<Dinosaur, Boolean> renderRequirement;
    private ResourceLocation textureLocation;

    public DinoLayer(String layerName, int basicLayer, Function<Dinosaur, Boolean> renderRequirement) {
        this.layerName = layerName;
        this.basicLayer = basicLayer;
        this.renderRequirement = renderRequirement;
    }

    public DinoLayer(String layerName, int basicLayer) {
        this(layerName,basicLayer ,(b) -> true);
    }

    public int getBasicLayer() {
        return basicLayer;
    }

    public ResourceLocation getTextureLocation(Dinosaur dino, double gender) {
            ResourceLocation dinoLoc = BuiltInRegistries.ENTITY_TYPE.getKey(dino.getType());
            if(gender == 1.0){
                textureLocation = new ResourceLocation(dinoLoc.getNamespace(), "textures/entity/" + dinoLoc.getPath() + "/female/" + layerName + ".png");
            } else if (gender == 2.0){
                textureLocation = new ResourceLocation(dinoLoc.getNamespace(), "textures/entity/" + dinoLoc.getPath() + "/male/" + layerName + ".png");
            }

        return textureLocation;
    }

    public String getLayerName() {
        return layerName;
    }

}
