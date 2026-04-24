package org.lightning.neutrontools.mixin.creativeTabs;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.jetbrains.annotations.Nullable;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;
import org.lightning.neutrontools.creativetabs.client.impl.ForgeTabData;
import org.lightning.neutrontools.mixin.creativeTabs.accessor.ForceCreativeTabAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 0)
public abstract class CreativeModeTabsMixin {

    @Shadow
    public static List<CreativeModeTab> allTabs() {
        return null;
    }



    /////////////////////////////////////////////
    /// Injections =========================== //
    /////////////////////////////////////////////
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
//        NeutronTools.TABS.populateSortedTabsList(allTabs());
//
//        for (CreativeModeTab tab : NeutronTools.TABS.orderedTabs) {
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
//        for (CreativeModeTab tab : NeutronTools.TABS.orderedTabs) {
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


}
