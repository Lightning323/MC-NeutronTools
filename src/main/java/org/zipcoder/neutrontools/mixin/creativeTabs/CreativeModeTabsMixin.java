package org.zipcoder.neutrontools.mixin.creativeTabs;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.creativetabs.NeutronCreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.impl.ForgeTabData;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.ForceCreativeTabAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 0)
public abstract class CreativeModeTabsMixin {

    @Shadow
    public static List<CreativeModeTab> allTabs() {
        return null;
    }

    @Shadow
    @Nullable
    private static CreativeModeTab.ItemDisplayParameters CACHED_PARAMETERS;


    /////////////////////////////////////////////
    /// Injections =========================== //
    /////////////////////////////////////////////

//    @Inject(method = "streamAllTabs", at = @At("RETURN"), cancellable = true)
//    private static void injectCustomTabs(CallbackInfoReturnable<Stream<CreativeModeTab>> cir) {
//        cir.setReturnValue(NeutronCreativeTabs.sortedTabs.stream());
//    }
//
//    // Supplementaries crashes the game with our tabs, since they are not registered (they are fake tabs)
//    // Work around to return the registered tabs only
//    @Inject(method = "tabs", at = @At("RETURN"), cancellable = true)
//    private static void injectTabsCompat(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
//        String thread = Thread.currentThread().getStackTrace()[3].getClassName();
//
//        if (!thread.isEmpty() && thread.toLowerCase().contains("supplementaries") && thread.toLowerCase().contains("modcreativetabs")) {
//            cir.setReturnValue(BuiltInRegistries.CREATIVE_MODE_TAB.stream().filter(CreativeModeTab::shouldDisplay).toList());
//        }
//    }
//
//    @Inject(method = "getDefaultTab", at = @At("RETURN"), cancellable = true)
//    private static void injectDefaultTab(CallbackInfoReturnable<CreativeModeTab> cir) {
//        if (allTabs() != null && !allTabs().isEmpty())
//            cir.setReturnValue(allTabs().get(0));
//    }
//
//    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
//    private static void injectValidation(CallbackInfo ci) {
//        ci.cancel();
//        int TABS_PER_PAGE = 10;
//        int count = 0;
//
//        record ItemGroupPosition(CreativeModeTab.Row row, int column, int page) {
//        }
//        var map = new HashMap<ItemGroupPosition, String>();
//
//        //Populate sorted tabs list
//        NeutronCreativeTabs.populateSortedTabsList(allTabs());
//
//        for (CreativeModeTab tab : NeutronCreativeTabs.sortedTabs) {
//            ForgeTabData forgeTab = (ForgeTabData) tab;
//            if (CreativeModeTabRegistry.getDefaultTabs().contains(tab)) {
//                forgeTab.setPageIndex(0);
//                continue;
//            }
//
//            final ForceCreativeTabAccessor itemGroupAccessor = (ForceCreativeTabAccessor) tab;
//            int pageIndex = count % TABS_PER_PAGE;
//            forgeTab.setPageIndex((count / TABS_PER_PAGE));
//            CreativeModeTab.Row row = pageIndex < (TABS_PER_PAGE / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
//            itemGroupAccessor.setRow(row);
//            itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE));
//
//            count++;
//        }
//        for (CreativeModeTab tab : NeutronCreativeTabs.sortedTabs) {
//            final ForgeTabData forgeTabData = (ForgeTabData) tab;
//            final String displayName = tab.getDisplayName().getString();
//            final var position = new ItemGroupPosition(tab.row(), tab.column(), forgeTabData.getPageIndex());
//            final String existingName = map.put(position, displayName);
//
//            if (existingName != null) {
//                throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, displayName, existingName));
//            }
//        }
//
//    }
//
//    @Inject(method = "tryRebuildTabContents", at = @At("HEAD"))
//    private static void injectReload(FeatureFlagSet arg, boolean bl, HolderLookup.Provider arg2, CallbackInfoReturnable<Boolean> cir) {
//        if (CreativeTabConfig.INSTANCE.isWasReloaded()) {
//            CACHED_PARAMETERS = null;
//            CreativeTabConfig.INSTANCE.setWasReloaded(false);
//        }
//    }

}
