package net.dumbcode.projectnublar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.species.DinosaurPart;
import net.dumbcode.projectnublar.entity.species.carnivore.TyrannosaurusRexEntity;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DinosaurRenderer extends GeoEntityRenderer<Dinosaur> {
    public DinosaurRenderer(EntityRendererProvider.Context renderManager, DefaultedEntityGeoModel model, List<DinoLayer> layers) {
        super(renderManager, model);
          for(DinoLayer layer : layers) {
           this.addRenderLayer(new GeoRenderLayer<>(this) {

            @Override
           public void render(PoseStack poseStack, Dinosaur animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
             Color color = animatable.layerColor(layers.indexOf(layer) + 1, layer);
             buffer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureResource(animatable)));
        reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), 1f);
                   reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, 1, 1, 1, 1);

                CoreGeoBone head = this.getGeoModel().getAnimationProcessor().getBone("head");

                if(head != null){
                    Vector3f local = new Vector3f(head.getPivotX(),head.getPivotY(),head.getPivotZ());
                    /*
                    Quaternionf q = new Quaternionf()
                            .rotateXYZ(head.getRotX(),head.getRotZ(),head.getRotX());
                    q.transform(local);
*/
                    Vec3 worldpos =  animatable.position().add(local.x,local.y,local.z);

                    animatable.setHeadPositon(worldpos);

                }

            }



         @Override
        public GeoModel<Dinosaur> getGeoModel() {
           return DinosaurRenderer.this.getGeoModel();
        }

         @Override
         protected ResourceLocation getTextureResource(Dinosaur animatable) {
           return layer.getTextureLocation(animatable, animatable.getDinoGender());
         }
         });
        }


    }



    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, Dinosaur animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float adultScale = ((float) animatable.getDinoData().getGeneValue(GeneInit.SIZE.get()) / 100) + 1.0f;
        float babyScale = adultScale * 0.25F;
        float juvenileScale = adultScale * 0.5F;
        float subAdultScale = adultScale * 0.75F;

        float renderScale = switch (animatable.getGrowthStage()) {
            case 1 -> babyScale;
            case 2 -> juvenileScale;
            case 3 -> subAdultScale;
            default -> adultScale;
        };

        super.scaleModelForRender(renderScale, renderScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    private Color color = null;

    @Override
    public Color getRenderColor(Dinosaur animatable, float partialTick, int packedLight) {
        return animatable.layerColor(0, null);
    }

    @Override
    public float getMotionAnimThreshold(Dinosaur animatable) {
        return 0.005f;
    }



    //    @Override
//    public RenderType getRenderType(Dinosaur animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
//        return RenderType.entityTranslucent(texture);
//    }
}
