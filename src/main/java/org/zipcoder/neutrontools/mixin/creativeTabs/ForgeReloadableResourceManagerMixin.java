package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ReloadableResourceManager.class)
public class ForgeReloadableResourceManagerMixin {

    //Called before the tags are loaded so this place is not a good place to reload the tabs
//    @Inject(method = "createReload", at = @At("RETURN"))
//    private void injectReload(Executor p_143930_, Executor p_143931_, CompletableFuture<Unit> p_143932_, List<PackResources> p_143933_, CallbackInfoReturnable<ReloadInstance> cir) {
//        CreativeTabs.reloadTabs();
//    }

}