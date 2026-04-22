package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreativeModeTabRegistry.class)
public abstract class ForgeCreativeTabRegistryMixin {

    /////////////////////////////////////////////
    /// Injections =========================== //
    /////////////////////////////////////////////

//    @Shadow
//    public static List<CreativeModeTab> getDefaultTabs() {
//        return null;
//    }
//
//    //https://www.bing.com/search?q=Unable+to+locate+obfuscation+mapping+for+%40Inject+target+getSortedCreativeModeTabs&cvid=0a15e365a2384902b52b45d60b343891&gs_lcrp=EgRlZGdlKgYIABBFGDkyBggAEEUYOdIBBzQyMmowajSoAgiwAgE&FORM=ANAB01&adppc=EDGEXST&PC=W093
//    @Inject(method = "getSortedCreativeModeTabs", at = @At("RETURN"), cancellable = true, remap = false)
//    private static void injectCustomTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
//        //First time caching of our original creative tabs
//        if (CreativeTabConfig.INSTANCE.original_SortedTabs == null)
//            CreativeTabConfig.INSTANCE.original_SortedTabs = cir.getReturnValue();
//
//        //This is the mixin where we exclude disabled tabs
//        List<CreativeModeTab> list = CreativeTabConfig.INSTANCE.sortedTabs.stream().filter(t -> !getDefaultTabs().contains(t)).toList();
//        cir.setReturnValue(list);
//    }

}
