package org.lightning.neutrontools.creativetabs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.CreativeTabsCache;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.getRegistryID;

public class NeutronCreativeTabs {

    public static final NeutronCreativeTabs INSTANCE = new NeutronCreativeTabs();
    //All data pertaining to the creative tabs, must be stored here
    public final CreativeTabsCache cache = new CreativeTabsCache();
    public final LinkedList<CreativeModeTab> orderedTabs = new LinkedList<>();
    public final HashMap<String, CreativeModeTab> newTabs = new HashMap<>();

    public NeutronCreativeTabs() {
    }

    public static void registerTabs(RegisterEvent event) {
        // Check if we are currently in the Creative Mode Tab registry phase
        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            INSTANCE.newTabs.forEach((tabKey, tab) -> {
                String originalTabKey = tabKey == null ? "unknown" : tabKey;
                try {
                    if (tabKey.contains(":")) {
                        tabKey = tabKey.split(":")[1];
                    }
                    //We need to replace all invalid characters
                    tabKey = tabKey.replaceAll("[^a-z0-9/._-]", "");

                    if (tabKey == null) {
                        LOGGER.error("Tab name key is null");
                        return;
                    }
                    LOGGER.info("Registering new tab {}", tabKey);
                    event.register(Registries.CREATIVE_MODE_TAB, NeutronTools.resource(tabKey), () -> tab);
                } catch (Exception e) {
                    LOGGER.error("Failed to register new tab \"{}\"", originalTabKey, e);
                }
            });
        }
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

        // 1. Process specific ordering
        for (String orderedTab : CreativeTabConfig.INSTANCE.tabOrder) {
            allTabs.stream()
                    .filter(tab -> CreativeTabUtils.getRegistryID(tab).equalsIgnoreCase(orderedTab))
                    .findFirst()
                    .ifPresent(pTab -> addTabToFilteredListIfNotDisabled(pTab, filteredTabs));
        }

        // 2. Process "existing" (catch-all for tabs not mentioned in tabOrder)
        for (CreativeModeTab tab : allTabs) {
            addTabToFilteredListIfNotDisabled(tab, filteredTabs);
        }


        // 3. Final safety for mandatory tabs (only adds if not already present)
        filteredTabs.addAll(MANDATORY_TABS);
        // 4. Update the final list
        orderedTabs.clear();
        orderedTabs.addAll(filteredTabs);

//        LOGGER.info("Populated sorted tabs list of {} total tabs", allTabs.size());
//        for (CreativeModeTab tab : sortedTabs) {
//            LOGGER.info("Sorted Tab: {}", tab.getDisplayName().getString());
//        }
    }

    private void addTabToFilteredListIfNotDisabled(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        //If our tab is not in the disabled tabs list, it makes it into the filtered list
        if (!CreativeTabConfig.INSTANCE.disabledTabs.contains(getRegistryID(tab))) {
            filteredTabs.add(tab);
        }
    }
}
