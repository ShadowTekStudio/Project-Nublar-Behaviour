package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.entity.HerbivoreDinosaur;
import net.dumbcode.projectnublar.entity.PackEntity;
import net.dumbcode.projectnublar.entity.social.interactionmanagers.tyrannosaurusrex.TyrannosaurInteractionEntity;
import net.dumbcode.projectnublar.entity.species.DinosaurPart;
import net.dumbcode.projectnublar.entity.species.carnivore.TyrannosaurusRexEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Constants.MODID, Registries.ENTITY_TYPE);
    public static final DeferredRegister<EntityType<?>> FAKE_ENTITIES = DeferredRegister.create(Constants.MODID, Registries.ENTITY_TYPE);
    public static final List<AttributesRegister<?>> attributeSuppliers = new ArrayList<>();

    public static final DeferredSupplier<EntityType<TyrannosaurusRexEntity>> TYRANNOSAURUS_REX = registerEntity("tyrannosaurus_rex", ()-> EntityType.Builder.of(TyrannosaurusRexEntity::new, MobCategory.MONSTER).sized(1,3), Dinosaur::createAttributes);
    public static final DeferredSupplier<EntityType<HerbivoreDinosaur>> TRICERATOPS = registerEntity("triceratops", ()-> EntityType.Builder.of(HerbivoreDinosaur::new, MobCategory.MONSTER).sized(2,3), Dinosaur::createAttributes);

    public static final DeferredSupplier<EntityType<TyrannosaurInteractionEntity>> INTERACTION_ENTITY = registerFakeEntity("carnivore_pack",()->EntityType.Builder.of(TyrannosaurInteractionEntity::new, MobCategory.MISC));
    public static final DeferredSupplier<EntityType<DinosaurPart>> DINOSAUR_PART = registerFakeEntity("dinosaur_part_entity", () -> EntityType.Builder.<DinosaurPart>of(DinosaurPart::new,
            MobCategory.MISC).sized(0.5f,0.5f));


    private static <T extends Entity> DeferredSupplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> supplier) {
        return ENTITIES.register(name, () -> supplier.get().build(Constants.MODID + ":" + name));
    }
    private static <T extends Entity> DeferredSupplier<EntityType<T>> registerFakeEntity(String name, Supplier<EntityType.Builder<T>> supplier) {
        return FAKE_ENTITIES.register(name, () -> supplier.get().build(Constants.MODID + ":" + name));
    }
    private static <T extends LivingEntity> DeferredSupplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> supplier,
                                                                                         Supplier<AttributeSupplier.Builder> attributeSupplier) {
        DeferredSupplier<EntityType<T>> entityTypeSupplier = registerEntity(name, supplier);
        attributeSuppliers.add(new AttributesRegister<>(entityTypeSupplier, attributeSupplier));
        return entityTypeSupplier;
    }

    public static void loadClass() {
        ENTITIES.register();
        FAKE_ENTITIES.register();
    }


    public record AttributesRegister<E extends LivingEntity>(Supplier<EntityType<E>> entityTypeSupplier, Supplier<AttributeSupplier.Builder> factory) {}
}
