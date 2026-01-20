package org.zipcoder.neutrontools.creativetabs;

import net.minecraft.world.item.CreativeModeTabs;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabCustomizationData;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import org.zipcoder.neutrontools.NeutronTools;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author HypherionSA
 */
//@Mod(ModConstants.MOD_ID)
public class CreativeTabs {

    private static boolean hasRun = false;

    public CreativeTabs() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (a, b) -> true));
    }

    /**
     * This is a client side method
     * Called to reload all creative tabs
     */
    public static void reloadTabs() {
        if (!hasRun) { //If this is our first time
            CreativeTabCustomizationData.INSTANCE.setVanillaTabs(new ArrayList<>(BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()));
            hasRun = true;
        }
        NeutronTools.LOGGER.info("Reloading creative tabs");
        long startTime = System.currentTimeMillis();
        CreativeTabCustomizationData.INSTANCE.clearTabs();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();

            //Find the json file that is under the new_tabs directory
            Map<ResourceLocation, Resource> customTabs = manager.listResources(NeutronTools.RESOURCE_ID, path -> path.getPath().endsWith(".json") && path.getPath().contains("new_tabs"));
            CreativeTabCustomizationData.INSTANCE.loadNewTabs(customTabs);

            Map<ResourceLocation, Resource> disabledItemsJson = manager.listResources(NeutronTools.RESOURCE_ID, path -> path.getPath().contains("disabled_items.json"));
            CreativeTabCustomizationData.INSTANCE.loadDisabledItems(disabledItemsJson);

            Map<ResourceLocation, Resource> disabledTabsJson = manager.listResources(NeutronTools.RESOURCE_ID, path -> path.getPath().contains("disabled_tabs.json"));
            CreativeTabCustomizationData.INSTANCE.loadDisabledTabs(disabledTabsJson);

            Map<ResourceLocation, Resource> orderedTabsJson = manager.listResources(NeutronTools.RESOURCE_ID, path -> path.getPath().contains("ordered_tabs.json"));
            CreativeTabCustomizationData.INSTANCE.loadOrderedTabs(orderedTabsJson);

            Map<ResourceLocation, Resource> itemsJson = manager.listResources(NeutronTools.RESOURCE_ID, path -> path.getPath().contains("tab_items.json"));
            CreativeTabCustomizationData.INSTANCE.loadItemsForTabs(itemsJson);

            //Update creative tabs after all information has been loaded
            CreativeTabCustomizationData.INSTANCE.reorderTabs();

            CreativeModeTabs.validate();

            //Rebuild cache for all tabs (mostly just icon cache)
            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                CreativeModeTabMixin_I mixinTab = (CreativeModeTabMixin_I) tab;
                mixinTab.rebuildCache();
            }

            //Log the final result
            StringBuilder sb = new StringBuilder();
            sb.append("Creative tabs reloaded ").append("(").append((System.currentTimeMillis() - startTime) / 1000).append("s elapsed time)\n")
                    .append(CreativeTabCustomizationData.INSTANCE.getNewTabs().size()).append(" New tabs\t")
                    .append(CreativeTabCustomizationData.INSTANCE.disabledTabs.size()).append(" Disabled tabs\t")
                    .append(CreativeTabCustomizationData.INSTANCE.getHiddenItems().size()).append(" Disabled items\t")
                    .append(CreativeTabCustomizationData.INSTANCE.tabAdditions.size()).append(" Tab additions\t")
                    .append(CreativeTabCustomizationData.INSTANCE.tabRemovals.size()).append(" Tab subtractions");
            NeutronTools.LOGGER.info(sb.toString());

        });
    }

    public static void refreshTabs() {
        if (!hasRun) { //If this is our first time
            reloadTabs();
            hasRun = true;
            return;
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                CreativeModeTabMixin_I mixinTab = (CreativeModeTabMixin_I) tab;
                mixinTab.reload();
            }
            CreativeModeTabs.validate();
            NeutronTools.LOGGER.info("Creative tabs have been refreshed");
        });
    }

}
