package org.zipcoder.creativetabs;

import org.zipcoder.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.creativetabs.client.tabs.CustomCreativeTabRegistry;
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
            CustomCreativeTabRegistry.INSTANCE.setVanillaTabs(new ArrayList<>(BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()));
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
        ModConstants.logger.info("Checking for custom creative tabs");
        CustomCreativeTabRegistry.INSTANCE.clearTabs();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();

            //Find the json file that does not contain "disabled_tabs" or "ordered_tabs", to create a new custom tab entry
            Map<ResourceLocation, Resource> customTabs = manager.listResources(ModConstants.RESOURCE_ID,
                    path -> path.getPath().endsWith(".json")
                            && !path.getPath().contains("disabled_tabs")
                            && !path.getPath().contains("ordered_tabs")
                            && !path.getPath().contains("tab_items"));//Added by me

            Map<ResourceLocation, Resource> disabledTabs = manager.listResources(ModConstants.RESOURCE_ID, path -> path.getPath().contains("disabled_tabs.json"));
            Map<ResourceLocation, Resource> orderedTabs = manager.listResources(ModConstants.RESOURCE_ID, path -> path.getPath().contains("ordered_tabs.json"));
            Map<ResourceLocation, Resource> items = manager.listResources(ModConstants.RESOURCE_ID, path -> path.getPath().contains("tab_items.json"));//Added by me


            //Load disabled tabs
            if (!disabledTabs.isEmpty()) {
                CustomCreativeTabRegistry.INSTANCE.loadDisabledTabs(disabledTabs);
            }

            //Load ordered tabs
            if (!orderedTabs.isEmpty()) {
                CustomCreativeTabRegistry.INSTANCE.loadOrderedTabs(orderedTabs);
            }

            //Process custom tabs
            CustomCreativeTabRegistry.INSTANCE.processEntries(customTabs);

            //Load items to remove or add
            if (!items.isEmpty()) {
                CustomCreativeTabRegistry.INSTANCE.addon.loadItemsForTabs(items);
            }

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
