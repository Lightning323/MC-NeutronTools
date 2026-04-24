package org.lightning.neutrontools.mixin.disableExpSettings;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.lightning.neutrontools.NeutronTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({CreateWorldScreen.class})
public class CreateWorldScreenMixin {
    @ModifyVariable(
            method = {"tryApplyNewDataPacks(Lnet/minecraft/server/packs/repository/PackRepository;ZLjava/util/function/Consumer;)V"},
            at = @At("HEAD"),
            argsOnly = true
    )
    public boolean dontShowWarning(boolean showWarning) {
        return !NeutronTools.CONFIG.disableExperementalSettings;
    }
}