package org.zipcoder.neutrontools.mixin;

import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zipcoder.neutrontools.NeutronTools;

@Mixin(value = PrimaryLevelData.class, remap = false)
public class PrimaryLevelDataMixinForge {

    /**
     * Stop experemental settings popup!
     * @param cir
     */
    @Inject(method = "hasConfirmedExperimentalWarning", at =@At("HEAD"), cancellable = true)
    public void hasConfirmedExperimentalWarning(CallbackInfoReturnable<Boolean> cir) {
        if(NeutronTools.CONFIG.disableExperementalSettings) {
            cir.setReturnValue(true);
        }
    }
}