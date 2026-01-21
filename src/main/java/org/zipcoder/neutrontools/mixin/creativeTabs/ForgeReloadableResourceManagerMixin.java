package org.zipcoder.neutrontools.mixin.creativeTabs;

import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zipcoder.neutrontools.events.TagEventHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManager.class)
public class ForgeReloadableResourceManagerMixin {

    //Called before the tags are loaded so this place is not a good place to reload the tabs
//    @Inject(method = "createReload", at = @At("RETURN"))
//    private void injectReload(Executor p_143930_, Executor p_143931_, CompletableFuture<Unit> p_143932_, List<PackResources> p_143933_, CallbackInfoReturnable<ReloadInstance> cir) {
//        CreativeTabs.reloadTabs();
//    }

}