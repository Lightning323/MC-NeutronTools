package org.lightning.neutrontools.mixin.keybinds;

import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.lightning.neutrontools.utils.KeybindUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin {

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/ArrayUtils;clone([Ljava/lang/Object;)[Ljava/lang/Object;")
    )
    private Object[] neutrontools$redirectCloneKeyArray(Object[] array) {
        return KeybindUtils.getVisibleKeyBindings();
    }
}