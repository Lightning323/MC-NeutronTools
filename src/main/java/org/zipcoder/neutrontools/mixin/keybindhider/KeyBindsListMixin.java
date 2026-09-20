package org.zipcoder.neutrontools.mixin.keybindhider;

import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.zipcoder.neutrontools.keybindhider.KeybindUtils;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/ArrayUtils;clone([Ljava/lang/Object;)[Ljava/lang/Object;"))
    private Object[] redirectClone(Object[] array) {
        return KeybindUtils.getVisibleKeyBindings();
    }
}