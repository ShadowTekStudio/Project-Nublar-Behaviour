package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.mixin.ItemOverridesMixin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class StackSensitiveItemOverrides<T extends IStackSensitive & BakedModel>{
    private final T model;
    private final ItemOverrides overrides;

    public StackSensitiveItemOverrides(T model) {
        this.model = model;
        this.overrides = ItemOverridesMixin.callCtor();

    }

}
