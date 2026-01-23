package org.zipcoder.neutrontools.mixin.creativeTabs;

import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabEdits;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeTabRegistry.class)
public abstract class ForgeCreativeTabRegistryMixin {

    @Shadow
    public static List<CreativeModeTab> getDefaultTabs() {
        return null;
    }

    //https://www.bing.com/search?q=Unable+to+locate+obfuscation+mapping+for+%40Inject+target+getSortedCreativeModeTabs&cvid=0a15e365a2384902b52b45d60b343891&gs_lcrp=EgRlZGdlKgYIABBFGDkyBggAEEUYOdIBBzQyMmowajSoAgiwAgE&FORM=ANAB01&adppc=EDGEXST&PC=W093
    @Inject(method = "getSortedCreativeModeTabs", at = @At("RETURN"), cancellable = true, remap = false)
    private static void injectCustomTabs(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        //First time caching of our original creative tabs
        if (CreativeTabEdits.INSTANCE.original_SortedTabs == null)
            CreativeTabEdits.INSTANCE.original_SortedTabs = cir.getReturnValue();

        //This is the mixin where we exclude disabled tabs
        if (CreativeTabEdits.INSTANCE.isEnabled() && CreativeTabEdits.INSTANCE.isWasReloadedFirstTime()) {
            List<CreativeModeTab> list = CreativeTabEdits.INSTANCE.sortedTabs.stream().filter(t -> !getDefaultTabs().contains(t)).toList();
            cir.setReturnValue(list);
        } else if (CreativeTabEdits.INSTANCE.original_SortedTabs != null) {
            cir.setReturnValue(CreativeTabEdits.INSTANCE.original_SortedTabs);
        }
    }

}
