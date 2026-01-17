package org.zipcoder.creativetabs;

import org.zipcoder.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.creativetabs.client.tabs.CreativeTabCustomizationData;
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
//        CustomCreativeTabRegistry.INSTANCE.setForge(true);
    }

    /**
     * This is a client side method
     */
    public static void reloadResources() {
        if (!hasRun) {
            CreativeTabCustomizationData.INSTANCE.setVanillaTabs(new ArrayList<>(BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()));
            reloadTabs();
            hasRun = true;
        } else {
            reloadTabs();
        }
    }

    /**
     * Called to reload all creative tabs
     */
    private static void reloadTabs() {
        NeutronTools.TAB_LOGGER.info("Checking for custom creative tabs");
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
        });
    }
}
