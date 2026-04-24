package org.lightning.neutrontools.creativetabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lightning.neutrontools.config.CreativeTabsCache;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.getRegistryID;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.getTranslationKey;

public class NeutronCreativeTabs {

    //All data pertaining to the creative tabs, must be stored here
    public final CreativeTabsCache cache = new CreativeTabsCache();
    public final LinkedList<CreativeModeTab> orderedTabs = new LinkedList<>();
    public final HashMap<String, CreativeModeTab> newTabs = new HashMap<>();
    public int builtContentsTabs;

    public void playerLoggedIn() {
        /**
         * Some variables do have to be reset when a player logs in
         * Otherwise, they will persist between sessions
         */
        builtContentsTabs = 0;
    }

    public NeutronCreativeTabs() {
    }

    public static final List<CreativeModeTab> MANDATORY_TABS = new ArrayList<>();

    static {
        MANDATORY_TABS.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab()));
        MANDATORY_TABS.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getHotbarTab()));
        MANDATORY_TABS.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getInventoryTab()));
    }

    public void populateSortedTabsList(Collection<CreativeModeTab> allTabs) {
        if (!orderedTabs.isEmpty() || allTabs.isEmpty()) return;//We only have to populate this once
//        System.out.println("All Tabs here : "+allTabs.stream().map(CreativeModeTab::getDisplayName).toList());

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
        filteredTabs.addAll(MANDATORY_TABS);
//        System.out.println("Filtered Tabs: " + filteredTabs.stream().map(CreativeModeTab::getDisplayName).toList());

        // 4. Update the final list
        orderedTabs.clear();
        orderedTabs.addAll(filteredTabs);

        LOGGER.info("Populated sorted tabs list of {} total tabs", allTabs.size());
//        for (CreativeModeTab tab : sortedTabs) {
//            LOGGER.info("Sorted Tab: {}", tab.getDisplayName().getString());
//        }
    }

    private void addTabToFilteredListIfNotDisabled(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        //If our tab is not in the disabled tabs list, it makes it into the filtered list
        if (!CreativeTabConfig.INSTANCE.disabledTabs.contains(getTranslationKey(tab)) &&
                !CreativeTabConfig.INSTANCE.disabledTabs.contains(getRegistryID(tab))) {
            filteredTabs.add(tab);
        }
    }
}
