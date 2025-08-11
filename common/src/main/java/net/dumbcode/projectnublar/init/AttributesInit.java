package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AttributesInit {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Constants.MODID, Registries.ATTRIBUTE);

    public static final DeferredSupplier<Attribute> DINO_HUNGER_NEED = ATTRIBUTES.register("dinosaur_hunger", () -> new RangedAttribute(
            // The translation key to use.
            "attributes.projectnublar.dinosaur_hunger",
            // The default value.
            250,
            // Min and max values.
            0,
            100000
    ).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_SOCIAL_NEED = ATTRIBUTES.register("dinosaur_social", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_social",250,0,100000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_THIRST_NEED = ATTRIBUTES.register("dinosaur_thirst", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_thirst", 250, 0, 100000).setSyncable(true));

    public static final DeferredSupplier<Attribute> DINO_ENERGY_NEED = ATTRIBUTES.register("dinosaur_stamina", () -> new RangedAttribute(
            "attributes.projectnublar.dinosaur_stamina", 250, 0, 100000).setSyncable(true));

    public static void loadClass(){ATTRIBUTES.register();}

}
