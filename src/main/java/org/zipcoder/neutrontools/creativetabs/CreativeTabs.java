package org.zipcoder.neutrontools.creativetabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreativeTabs {

    private static boolean hasRun = false;

    public CreativeTabs() {
    }

    /**
     * This is a client side method
     * Called to reload all creative tabs
     */
    public static void reloadTabs() {
        if (!hasRun) {
            //Do this the first time
            CreativeTabConfig.INSTANCE.setVanillaTabs(new ArrayList<>(BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()));
            hasRun = true;
        }
        CreativeTabConfig.INSTANCE.load();
    }

    private static void load(long startTime) {
        CreativeTabConfig.INSTANCE.load();
        CreativeModeTabs.validate();

        //reset cache for all tabs
        itemsFromUnregisteredTabs.clear();
        for (CreativeModeTab tab : CreativeTabConfig.INSTANCE.newTabs) {  //Do Unregistered tabs first!
            if (CreativeTabConfig.INSTANCE.tabAdditions.get(tab) != null)
                itemsFromUnregisteredTabs.addAll(CreativeTabConfig.INSTANCE.tabAdditions.get(tab).getAllItemStacks());
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
    }


    //We need to add the items from unregistered tabs to the search tab otherwise they will not show up in the search
    final static Set<ItemStack> itemsFromUnregisteredTabs = new HashSet<>();


    public static Set<ItemStack> getItemsFromUnregisteredTabs() {
        return itemsFromUnregisteredTabs;
    }

}
