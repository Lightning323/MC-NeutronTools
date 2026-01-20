package org.zipcoder.neutrontools.creativetabs;

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
        CreativeTabCustomizationData.INSTANCE.clearTabs();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();

            //Find the json file that is under the new_tabs directory
            Map<ResourceLocation, Resource> customTabs = manager.listResources(NeutronTools.RESOURCE_ID, path -> path.getPath().endsWith(".json") && path.getPath().contains("new_tabs"));
            CreativeTabCustomizationData.INSTANCE.loadCustomTabs(customTabs);

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

            //Rebuild cache for all tabs
            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                NeutronTools.LOGGER.info("Building cache for '{}'", tab.getDisplayName().getString());
                // BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab)
                CreativeModeTabMixin_I mixinTab = (CreativeModeTabMixin_I) tab;
                mixinTab.rebuildCache();
            }

            //Log the final result
            StringBuilder sb = new StringBuilder();
            sb.append("Creative tabs have been reloaded:\n")
                    .append(CreativeTabCustomizationData.INSTANCE.getNewTabs().size()).append(" New tabs\n")
                    .append(CreativeTabCustomizationData.INSTANCE.disabledTabs.size()).append(" Disabled tabs\n")
                    .append(CreativeTabCustomizationData.INSTANCE.disabledItems.size()).append(" Disabled items\n")
                    .append(CreativeTabCustomizationData.INSTANCE.tabAdditions.size()).append(" Tab additions\n")
                    .append(CreativeTabCustomizationData.INSTANCE.tabDeletions.size()).append(" Tab subtractions\n");
            NeutronTools.LOGGER.info(sb.toString());

        });
    }
}
