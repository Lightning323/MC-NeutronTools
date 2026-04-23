package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.zipcoder.neutrontools.NeutronTools.CONFIG_PATH;
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
    public final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();
    public final LinkedHashSet<CreativeModeTab> newTabs = new LinkedHashSet<>();
    public final HashMap<CreativeModeTab, ItemAdditionList> tabAdditions = new HashMap<>();
    public final HashMap<CreativeModeTab, Set<Item>> tabRemovals = new HashMap<>();
    public final Set<String> disabledTabs = new HashSet<>();
    public final Set<Item> disabledItems = new HashSet<>();
    private boolean wasReloaded = false;

    public static void plantStarterFiles() {
        try {
            Files.writeString(new File(CONFIG_PATH, "disabled_tabs.json").toPath(),
                    "{\n\"disabled_tabs\":[]\n}");
            Files.writeString(new File(CONFIG_PATH, "disabled_items.json").toPath(),
                    "{\n\"disabled_items\":[]\n}");
            Files.writeString(new File(CONFIG_PATH, "ordered_tabs.json").toPath(),
                    "{\n\"ordered_tabs\":[]\n}");
            new File(CONFIG_PATH, "new_tabs").mkdirs();
            new File(CONFIG_PATH, "tab_items").mkdirs();
        } catch (Exception e) {
            NeutronTools.LOGGER.error("Failed to plant starter files", e);
        }
    }


    public void load() {
        NeutronTools.LOGGER.debug("Loading Creative Tab Config");
        //Reset everything first
        wasReloaded = true;
        newTabs.clear();
        disabledItems.clear();
        disabledTabs.clear();
        tabAdditions.clear();
        tabOrder.clear();
        replacedTabs.clear();
        tabRemovals.clear();

        loadCustomTabItems(new File(CONFIG_PATH, "tab_items.json"));
        File[] subfiles = new File(CONFIG_PATH, "tab_items").listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json")) {
                    loadCustomTabItems(tabEditFile);
                }
            }
        }

        loadSimpleJsonLists(new File(CONFIG_PATH, "disabled_tabs.json"));
        loadSimpleJsonLists(new File(CONFIG_PATH, "disabled_items.json"));
        loadSimpleJsonLists(new File(CONFIG_PATH, "ordered_tabs.json"));

        //Add disabled items from JEI
        if (NeutronTools.CONFIG.hideCreativeTabItemsFromJEIBlacklist) {
            Path configDir = FMLPaths.CONFIGDIR.get();
            File jeiBlacklist = new File(configDir.toFile(), "jei/blacklist.cfg");
            if (jeiBlacklist.exists()) {
                try {
                    Files.readAllLines(jeiBlacklist.toPath()).forEach(line -> {
                        if (!line.isBlank()) {
                            Item i = makeItemStack(line.strip()).getItem();
                            disabledItems.add(i);
                        }
                    });
                } catch (IOException e) {
                    NeutronTools.LOGGER.warn("Failed to process JEI blacklisted items {}", e);
                }
            }
        }


        LOGGER.debug("Creative Tab Config loaded");
        LOGGER.debug("Disabled tabs: {}", disabledTabs);
        LOGGER.debug("Ordered tabs: {}", tabOrder);
//        LOGGER.debug("New tabs: {}", newTabs);
//        LOGGER.debug("Tab additions: {}", tabAdditions);
//        LOGGER.debug("Tab removals: {}", tabRemovals);
//        LOGGER.debug("Replaced tabs: {}", replacedTabs);
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

    public void loadCustomTabItems(File file) {
        if (!Files.exists(file.toPath())) {
            return;
        }
        JsonObject jsonObject;
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            NeutronTools.LOGGER.warn("Failed to parse config file: {}", file);
            return;
        }

        if (jsonObject.has("tabs")) {
            for (JsonElement t : jsonObject.getAsJsonArray("tabs")) {
                try {
                    JsonObject tabJson = t.getAsJsonObject();
                    String tabName = tabJson.get("tab_name").getAsString();
                    CreativeModeTab tab = CreativeTabUtils.getTabFromString(tabName);

                    JsonArray itemsAdd = tabJson.get("items_to_add").getAsJsonArray();
                    JsonArray itemsRemove = tabJson.get("items_to_remove").getAsJsonArray();

                    if (tab != null) {
                        itemsRemove.forEach(item -> {
                            tabRemovals.computeIfAbsent(tab, k -> new HashSet<>()).add(makeItemStack(item.getAsString()).getItem());
                        });
                        itemsAdd.forEach(json -> {

                            // Initialize maps if they don't exist
                            tabAdditions.computeIfAbsent(tab, k -> new ItemAdditionList());
                            tabRemovals.computeIfAbsent(tab, k -> new HashSet<>());

                            // Process Deletions
                            Set<Item> thisTabDeletions = tabRemovals.get(tab);
                            if (thisTabDeletions != null && itemsRemove != null) {
                                for (JsonElement tabItem : itemsRemove) {
                                    Item itemToDelete = CreativeTabUtils.getItemByName(tabItem.getAsString());
                                    if (itemToDelete != null) thisTabDeletions.add(itemToDelete);
                                }
                            }

                            // Process Additions
                            ItemAdditionList thisTabAdditions = tabAdditions.get(tab);
                            if (thisTabAdditions != null && itemsAdd != null) {
                                for (JsonElement tabItemJson : itemsAdd) {
                                    // 1. Deserialize the JsonElement into your TabItem class
                                    TabItem tabItem = GSON.fromJson(tabItemJson, TabItem.class);
                                    tabItem.populateAdditions(thisTabAdditions);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    NeutronTools.LOGGER.warn("Failed to process items in creative tab entry", e);
                }
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
        if (!Files.exists(file.toPath())) {
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
                    disabledItems.add(i);
                });
            }
        }
    }


}
