package org.zipcoder.neutrontools.mixin.keybindhider;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.zipcoder.neutrontools.keybindhider.KeybindUtils;

@Pseudo
@Mixin(targets = "com.blamejared.controlling.client.NewKeyBindsList")
public class ControllingKeyBindsListMixin {

    @ModifyVariable(method = "<init>", at = @At(value = "STORE", ordinal = 0), require = 0)
    private KeyMapping[] modifyBindings(KeyMapping[] original) {
        return KeybindUtils.getVisibleKeyBindings();
    }
}