package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraftforge.client.CreativeModeTabSearchRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zipcoder.neutrontools.NeutronTools;

@Mixin(CreativeModeInventoryScreen.class)
public class ForgeCreativeModeInventoryMixin {

    @Inject(method = "init", at = @At("HEAD"))
    private void injectCreativeTabs(CallbackInfo ci) {
        //TODO: To combat java.lang.IllegalStateException: Tree builder not registered crash
        if (Minecraft.getInstance().getSearchTree(SearchRegistry.CREATIVE_NAMES) == null) {
            NeutronTools.LOGGER.info("Search trees are null, Creating search trees");
            CreativeModeTabSearchRegistry.createSearchTrees();
        }
    }


}
