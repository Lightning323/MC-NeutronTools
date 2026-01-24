package org.zipcoder.neutrontools.creativetabs;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabEdits;
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

import java.util.*;

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
            CreativeTabEdits.INSTANCE.setVanillaTabs(new ArrayList<>(BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()));
            hasRun = true;
        }
        NeutronTools.LOGGER.info("Reloading creative tabs");
        long startTime = System.currentTimeMillis();
        CreativeTabEdits.INSTANCE.clearTabs();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {

            if(CreativeTabEdits.INSTANCE.isEnabled()) {
                ResourceManager manager = Minecraft.getInstance().getResourceManager();

                //Find the json file that is under the new_tabs directory
                Map<ResourceLocation, Resource> customTabs = manager.listResources(NeutronTools.RESOURCE_ID, path ->
                        path.getPath().endsWith(".json") && path.getPath().contains("new_tabs"));
                CreativeTabEdits.INSTANCE.loadNewTabs(customTabs);

                Map<ResourceLocation, Resource> disabledItemsJson = manager.listResources(NeutronTools.RESOURCE_ID, path ->
                        path.getPath().endsWith("disabled_items.json"));
                CreativeTabEdits.INSTANCE.loadDisabledItems(disabledItemsJson);

                Map<ResourceLocation, Resource> disabledTabsJson = manager.listResources(NeutronTools.RESOURCE_ID, path ->
                        path.getPath().endsWith("disabled_tabs.json"));
                CreativeTabEdits.INSTANCE.loadDisabledTabs(disabledTabsJson);

                Map<ResourceLocation, Resource> orderedTabsJson = manager.listResources(NeutronTools.RESOURCE_ID, path ->
                        path.getPath().endsWith("ordered_tabs.json"));
                CreativeTabEdits.INSTANCE.loadOrderedTabs(orderedTabsJson);

                Map<ResourceLocation, Resource> itemsJson = manager.listResources(NeutronTools.RESOURCE_ID, path ->
                        path.getPath().endsWith("tab_items.json")
                                || (path.getPath().endsWith(".json") && path.getPath().contains("tab_items")));
                CreativeTabEdits.INSTANCE.loadItemsForTabs(itemsJson);
            }
            //Update creative tabs after all information has been loaded
            CreativeTabEdits.INSTANCE.reorderTabs_indexSortedTabs();

            CreativeModeTabs.validate();

            //reset cache for all tabs
            itemsFromUnregisteredTabs.clear();
            for (CreativeModeTab tab : CreativeTabEdits.INSTANCE.newTabs) {  //Do Unregistered tabs first!
                if (CreativeTabEdits.INSTANCE.tabAdditions.get(tab) != null)
                    itemsFromUnregisteredTabs.addAll(CreativeTabEdits.INSTANCE.tabAdditions.get(tab).getAllItemStacks());
                CreativeModeTabMixin_I mixinTab = (CreativeModeTabMixin_I) tab;
                mixinTab.resetCache();
            }
            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                CreativeModeTabMixin_I mixinTab = (CreativeModeTabMixin_I) tab;
                mixinTab.resetCache();
            }


            //Log the final result
            StringBuilder sb = new StringBuilder();
            sb.append("Creative tabs reloaded ").append("(").append((System.currentTimeMillis() - startTime) / 1000).append("s elapsed time)\n");
            NeutronTools.LOGGER.info(sb.toString());

        });
    }

    //We need to add the items from unregistered tabs to the search tab otherwise they will not show up in the search
    final static Set<ItemStack> itemsFromUnregisteredTabs = new HashSet<>();


    public static Set<ItemStack> getItemsFromUnregisteredTabs() {
        return itemsFromUnregisteredTabs;
    }

    public static void refreshTabs() {
        if (!hasRun) { //If this is our first time
            reloadTabs();
            hasRun = true;
            return;
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            CreativeModeTabs.validate();
            NeutronTools.LOGGER.info("Creative tabs have been refreshed");
        });
    }
}
