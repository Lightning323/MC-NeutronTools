package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.zipcoder.neutrontools.NeutronTools.CONFIGDIR;
import static org.zipcoder.neutrontools.NeutronTools.LOGGER;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.*;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
public class CreativeTabConfig {

    public CreativeTabConfig() {
        load();
        mandatoryTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab()));
        mandatoryTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getHotbarTab()));
        mandatoryTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getInventoryTab()));
    }

    public static final CreativeTabConfig INSTANCE = new CreativeTabConfig();
    protected final Gson GSON = new Gson();

    //Our config parameters
    public final List<CreativeModeTab> mandatoryTabs = new ArrayList<>();
    private final List<CreativeModeTab> vanillaTabs = new ArrayList<>();
    public final LinkedList<CreativeModeTab> sortedTabs = new LinkedList<>();
    private final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();
    public final LinkedHashSet<CreativeModeTab> newTabs = new LinkedHashSet<>();
    public final HashMap<CreativeModeTab, ItemAdditionList> tabAdditions = new HashMap<>();
    public final HashMap<CreativeModeTab, Set<Item>> tabRemovals = new HashMap<>();
    public final Set<String> disabledTabs = new HashSet<>();
    public final Set<Item> hiddenItems = new HashSet<>();
    public final Set<Item> priorityHiddenItems = new HashSet<>();
    private boolean wasReloaded = false;

    //For caching the original state of the creative tabs
    public List<CreativeModeTab> original_SortedTabs;
    public final HashMap<CreativeModeTab, List<ItemStack>> original_tabDisplayItems = new HashMap<>();


    public void load() {
        NeutronTools.LOGGER.debug("Loading Creative Tab Config");
        //Reset everything first
        wasReloaded = true;
        newTabs.clear();
        hiddenItems.clear();
        disabledTabs.clear();
        tabAdditions.clear();
        tabOrder.clear();
        sortedTabs.clear();
        replacedTabs.clear();
        tabRemovals.clear();

        //CreativeTabConfig.INSTANCE.loadNewTabs(customTabs);
        //CreativeTabConfig.INSTANCE.loadItemsForTabs(itemsJson);
        loadSimpleJsonLists(new File(CONFIGDIR, "disabled_tabs.json"));
        loadSimpleJsonLists(new File(CONFIGDIR, "disabled_items.json"));
        loadSimpleJsonLists(new File(CONFIGDIR, "ordered_tabs.json"));

        //Add disabled items from JEI
        if (NeutronTools.CONFIG.hideCreativeTabItemsFromJEIBlacklist) {
            Path configDir = FMLPaths.CONFIGDIR.get();
            File jeiBlacklist = new File(configDir.toFile(), "jei/blacklist.cfg");
            if (jeiBlacklist.exists()) {
                try {
                    Files.readAllLines(jeiBlacklist.toPath()).forEach(line -> {
                        if (!line.isBlank()) {
                            Item i = makeItemStack(line.strip()).getItem();
                            hiddenItems.add(i);
                            priorityHiddenItems.add(i);
                        }
                    });
                } catch (IOException e) {
                    NeutronTools.LOGGER.warn("Failed to process JEI blacklisted items {}", e);
                }
            }
        }

//        reorderTabs_indexSortedTabs();

        LOGGER.debug("Creative Tab Config loaded");
        LOGGER.debug("Disabled tabs: {}", disabledTabs);
    }



    public void setWasReloaded(boolean b) {
        wasReloaded = b;
    }

    public boolean isWasReloaded() {
        return wasReloaded;
    }

    public Pair<NewTabJsonHelper, ItemAdditionList> getReplacementTab(CreativeModeTab tab) {
        Pair<NewTabJsonHelper, ItemAdditionList> newTabJsonHelperListPair = replacedTabs.get(getTranslationKey(tab));
        if (newTabJsonHelperListPair != null) {
            return newTabJsonHelperListPair;
        }
        newTabJsonHelperListPair = replacedTabs.get(getRegistryID(tab));
        return newTabJsonHelperListPair;
    }


    public final HashMap<String, Pair<NewTabJsonHelper, ItemAdditionList>> replacedTabs = new HashMap<>();

    public boolean isTabDisabled(CreativeModeTab self) {
        return disabledTabs.contains(CreativeTabUtils.getRegistryID(self)) ||
                disabledTabs.contains(CreativeTabUtils.getTranslationKey(self));
    }

    public enum TabNameMode {
        NORMAL, TRANSLATION_KEY, RESOURCE_ID
    }

    private TabNameMode tabNameMode = TabNameMode.NORMAL;

    public void setTabNameMode(TabNameMode tabNameMode) {
        this.tabNameMode = tabNameMode;
    }

    public TabNameMode getTabNameMode() {
        return tabNameMode;
    }

    public void loadItemsForTabs(JsonObject itemsJson) {
        if (itemsJson == null || itemsJson.size() == 0) return;

        // Iterate over each member in the root JsonObject
        for (Map.Entry<String, JsonElement> entry : itemsJson.entrySet()) {
            String entryKey = entry.getKey();
            JsonElement element = entry.getValue();

            NeutronTools.LOGGER.info("Processing tab item data for key: {}", entryKey);

            try {
                // Directly parse the JsonElement into your helper class using GSON
                TabItemsJsonHelper helper = GSON.fromJson(element, TabItemsJsonHelper.class);

                if (helper.getTabs() == null) continue;

                helper.getTabs().forEach(json -> {
                    CreativeModeTab tab = CreativeTabUtils.getTabFromString(json.tabName);

                    if (tab != null) {
                        // Initialize maps if they don't exist
                        tabAdditions.computeIfAbsent(tab, k -> new ItemAdditionList());
                        tabRemovals.computeIfAbsent(tab, k -> new HashSet<>());

                        // Process Additions
                        ItemAdditionList thisTabAdditions = tabAdditions.get(tab);
                        if (thisTabAdditions != null && json.itemsAdd != null) {
                            for (TabItem tabItem : json.itemsAdd) {
                                tabItem.populateAdditions(thisTabAdditions);
                            }
                        }

                        // Process Deletions
                        Set<Item> thisTabDeletions = tabRemovals.get(tab);
                        if (thisTabDeletions != null && json.itemsRemove != null) {
                            for (TabItem tabItem : json.itemsRemove) {
                                thisTabDeletions.addAll(tabItem.makeItemsForRemoval());
                            }
                        }
                    }
                });
            } catch (Exception e) {
                NeutronTools.LOGGER.warn("Failed to process items in creative tab entry: " + entryKey, e);
            }
        }
    }

    public void loadNewTab(JsonObject root) {
        if (root == null) return;

        // Iterate through every entry inside the main JsonObject
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String tabId = entry.getKey();

            // Ensure the element is an object before processing
            if (!entry.getValue().isJsonObject()) continue;
            JsonObject tabJson = entry.getValue().getAsJsonObject();

            NeutronTools.LOGGER.info("Processing tab data for: {}", tabId);

            try {
                // Map the specific tab's JsonObject to your helper class
                NewTabJsonHelper json = GSON.fromJson(tabJson, NewTabJsonHelper.class);
                ItemAdditionList additionList = new ItemAdditionList();

                if (!json.isTabEnabled()) continue;

                // 1. Process items to add
                if (json.itemsToAdd != null) {
                    for (TabItem item : json.itemsToAdd) {
                        if (item.name != null && item.name.equalsIgnoreCase("existing")) {
                            json.setKeepExisting(item.index);
                        }
                        item.populateAdditions(additionList);
                    }
                }

                // 2. Process items to remove
                if (json.itemsToRemove != null) {
                    for (TabItem item : json.itemsToRemove) {
                        Set<Item> removeItems = item.makeItemsForRemoval();
                        additionList.removeStacksIf((stack) -> removeItems.contains(stack.getItem()));
                    }
                }

                // 3. Tab Replacement or Construction
                if (json.replaceTab != null && !json.replaceTab.isBlank()) {
                    NeutronTools.LOGGER.info("Replaced Tab {} with {}", tabId, json.replaceTab);
                    replacedTabs.put(json.replaceTab, Pair.of(json, additionList));
                } else {
                    CreativeModeTab.Builder builder = CreativeModeTab.builder();

                    // Use tabId from the JSON key if the helper name is null
                    String titleKey = json.getTabName() != null ? json.getTabName() : tabId;
                    builder.title(Component.translatable(prefix(titleKey)));

                    builder.icon(makeTabIcon(json));

                    if (json.getTabBackground() != null && !json.getTabBackground().isEmpty()) {
                        builder.backgroundTexture(CreativeModeTab.createTextureLocation(json.getTabBackground()));
                    }

                    CreativeModeTab tab = builder.build();
                    newTabs.add(tab);
                    tabAdditions.put(tab, additionList);
                }
            } catch (Exception e) {
                NeutronTools.LOGGER.warn("Failed to process creative tab: " + tabId, e);
            }
        }
    }

    public void loadSimpleJsonLists(File file) {
        if (!Files.exists(file.toPath())){
            return;
        }
        JsonObject jsonObject;
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            NeutronTools.LOGGER.warn("Failed to parse config file: {}", file);
            return;
        }

        if (!jsonObject.isEmpty()) {
            if (jsonObject.has("disabled_tabs")) {
                jsonObject.getAsJsonArray("disabled_tabs").forEach(e -> {
                    disabledTabs.add(e.getAsString());
                });
            }
            if (jsonObject.has("ordered_tabs")) {
                jsonObject.getAsJsonArray("ordered_tabs").forEach(e -> {
                    tabOrder.add(e.getAsString());
                });
            }
            if (jsonObject.has("disabled_items")) {
                jsonObject.getAsJsonArray("disabled_items").forEach(e -> {
                    Item i = makeItemStack(e.getAsString()).getItem();
                    hiddenItems.add(i);
                    priorityHiddenItems.add(i);
                });
            }
        }
    }


    public void reorderTabs_indexSortedTabs() {
        List<CreativeModeTab> allTabs = new ArrayList<>();
        allTabs.addAll(vanillaTabs);
        allTabs.addAll(newTabs);

        LinkedHashSet<CreativeModeTab> filteredTabs = new LinkedHashSet<>();
        boolean addRemaining = false;

        // 1. Process specific ordering
        for (String orderedTab : tabOrder) {
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
        if (addRemaining || tabOrder.isEmpty()) {
            for (CreativeModeTab tab : allTabs) {
                addTabToFilteredListIfNotDisabled(tab, filteredTabs);
            }
        }

        // 3. Final safety for mandatory tabs (only adds if not already present)
        filteredTabs.addAll(mandatoryTabs);

        // 4. Update the final list
        sortedTabs.clear();
        sortedTabs.addAll(filteredTabs);
    }


    private void addTabToFilteredListIfNotDisabled(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        //If our tab is not in the disabled tabs list, it makes it into the filtered list
        if (!disabledTabs.contains(getTranslationKey(tab)) &&
                !disabledTabs.contains(getRegistryID(tab))) {
            filteredTabs.add(tab);
        }
    }

    public void setVanillaTabs(List<CreativeModeTab> tabs) {
        this.vanillaTabs.clear();
        this.vanillaTabs.addAll(tabs);
    }

}
