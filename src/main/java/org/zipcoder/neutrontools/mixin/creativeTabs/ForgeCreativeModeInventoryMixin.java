package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public class ForgeCreativeModeInventoryMixin {

    @Inject(method = "init", at = @At("HEAD"))
    private void injectCreativeTabs(CallbackInfo ci) {
        //CreativeModeTabSearchRegistry.createSearchTrees();
    }

}
