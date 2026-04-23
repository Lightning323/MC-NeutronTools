package org.zipcoder.neutrontools.creativetabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getRegistryID;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getTranslationKey;

public class NeutronCreativeTabs {

    //We need to add the items from unregistered tabs to the search tab otherwise they will not show up in the search
    final static Set<ItemStack> itemsFromUnregisteredTabs = new HashSet<>();
    public static final HashMap<String, Collection<ItemStack>> cached_creativeTabs = new HashMap<>();
    public static final HashMap<String, Collection<ItemStack>> cached_originalCreativeTabs = new HashMap<>();
    public static final LinkedList<CreativeModeTab> sortedTabs = new LinkedList<>();


    public static Set<ItemStack> getItemsFromUnregisteredTabs() {
        return itemsFromUnregisteredTabs;
    }

    public static void playerLoggedIn() {
        itemsFromUnregisteredTabs.clear();
        cached_creativeTabs.clear();
        cached_originalCreativeTabs.clear();
    }


    public static void populateSortedTabsList(Collection<CreativeModeTab> allTabs) {
        if (!sortedTabs.isEmpty() || allTabs.isEmpty()) return;//We only have to populate this once


        LinkedHashSet<CreativeModeTab> filteredTabs = new LinkedHashSet<>();
        boolean addRemaining = false;

        // 1. Process specific ordering
        for (String orderedTab : CreativeTabConfig.INSTANCE.tabOrder) {
            if (orderedTab.equalsIgnoreCase("existing")) {
                addRemaining = true;
                continue;
            }

            allTabs.stream()
                    .filter(tab -> {
                        String key = getTranslationKey(tab);
                        if (key.equalsIgnoreCase(orderedTab)
                                || key.replace("itemGroup.", "").equalsIgnoreCase(orderedTab))
                            return true;

                        if (CreativeTabUtils.getRegistryID(tab).equalsIgnoreCase(orderedTab)) return true;

                        return false;
                    })
                    .findFirst()
                    .ifPresent(pTab -> addTabToFilteredListIfNotDisabled(pTab, filteredTabs));
        }

        // 2. Process "existing" (catch-all for tabs not mentioned in tabOrder)
        if (addRemaining || CreativeTabConfig.INSTANCE.tabOrder.isEmpty()) {
            for (CreativeModeTab tab : allTabs) {
                addTabToFilteredListIfNotDisabled(tab, filteredTabs);
            }
        }

        // 3. Final safety for mandatory tabs (only adds if not already present)
        filteredTabs.addAll(CreativeTabConfig.INSTANCE.mandatoryTabs);

        // 4. Update the final list
        sortedTabs.clear();
        sortedTabs.addAll(filteredTabs);

        LOGGER.info("Populated sorted tabs list of {} total all tabs", allTabs.size());
        for (CreativeModeTab tab : sortedTabs) {
            LOGGER.info("Sorted Tab: {}", tab.getDisplayName().getString());
        }
    }

    private static void addTabToFilteredListIfNotDisabled(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        //If our tab is not in the disabled tabs list, it makes it into the filtered list
        if (!CreativeTabConfig.INSTANCE.disabledTabs.contains(getTranslationKey(tab)) &&
                !CreativeTabConfig.INSTANCE.disabledTabs.contains(getRegistryID(tab))) {
            filteredTabs.add(tab);
        }
    }
}
