package net.dumbcode.projectnublar.mixin;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemOverrides.class)
public class ItemOverridesMixin {

    @Invoker("<init>")
    public static ItemOverrides callCtor() {
        throw new AssertionError(); // replaced by Mixin
    }
}
