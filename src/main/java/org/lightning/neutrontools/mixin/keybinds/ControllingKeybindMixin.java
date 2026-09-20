package org.lightning.neutrontools.mixin.keybinds;

import net.minecraft.client.KeyMapping;
import org.lightning.neutrontools.utils.KeybindUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.blamejared.controlling.client.NewKeyBindsList")
public abstract class ControllingKeybindMixin {

    @ModifyVariable(method = "<init>", at = @At(value = "STORE", ordinal = 0))
    private KeyMapping[] neutrontools$modifyKeyBindings(KeyMapping[] original) {
        return KeybindUtils.getVisibleKeyBindings();
    }
}