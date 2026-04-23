package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.zipcoder.neutrontools.NeutronTools.CONFIG_PATH;
import static org.zipcoder.neutrontools.NeutronTools.LOGGER;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.makeItemStack;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
public class CreativeTabConfig {
    public CreativeTabConfig() {
        load();
    }

    public static final CreativeTabConfig INSTANCE = new CreativeTabConfig();

    public final Set<String> disabledTabs = new HashSet<>();
    public final Set<Item> disabledItems = new HashSet<>();

    public final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();

    public HashMap<CreativeModeTab, TabEditConfig> newTabs = new HashMap<>();
    public HashMap<CreativeModeTab, TabEditConfig> tabEdits = new HashMap<>();

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
            new File(CONFIG_PATH, "tab_edits").mkdirs();

            String tabEditsJson =
                    """
                                {
                                "minecraft:functional_blocks": {
                                    "tab_icon": {
                                      "name": "minecraft:apple",
                                      "nbt": ""
                                    },
                                    "tab_name_key": "Functional Blocks",
                                    "items_to_add": [
                                      {
                                        "index": 0,
                                        "names": ["minecraft:apple", "minecraft:bamboo"],
                                        "match_name": "regex",
                                        "match_tags": ["tag"],
                                        "match_tab": "tab_id",
                                        "nbt": ""
                                      }
                                    ],
                                    "items_to_remove": [
                                      {
                                        "names": ["minecraft:apple","minecraft:bamboo"],
                                        "match_name": "regex",
                                        "match_tags": ["tag"],
                                      }
                                    ]
                                }
                            }
                            """;

            Files.writeString(new File(CONFIG_PATH, "tab_edits/custom_tab_edits.json").toPath(), tabEditsJson);
        } catch (Exception e) {
            NeutronTools.LOGGER.error("Failed to plant starter files", e);
        }
    }

    public void load() {
        NeutronTools.LOGGER.debug("Loading Creative Tab Config");
        wasReloaded = true;

        //-------------------------------------------------------
        //Load simple lists
        //-------------------------------------------------------
        disabledItems.clear();
        disabledTabs.clear();
        tabOrder.clear();
        loadSimpleJsonLists(new File(CONFIG_PATH, "disabled_tabs.json"));
        loadSimpleJsonLists(new File(CONFIG_PATH, "disabled_items.json"));
        loadSimpleJsonLists(new File(CONFIG_PATH, "ordered_tabs.json"));

        //-------------------------------------------------------
        //Add disabled items from JEI
        //-------------------------------------------------------
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
        LOGGER.debug("Disabled tabs: {}", disabledTabs);
        LOGGER.debug("Ordered tabs: {}", tabOrder);

        //-------------------------------------------------------
        //Load tab edits
        //-------------------------------------------------------
        HashMap<String, TabEditJsonRepresentation> jsonTabEdits = new HashMap<>();
        TabEditJsonRepresentation.load(new File(CONFIG_PATH, "tab_items.json"), jsonTabEdits);
        File[] subfiles = new File(CONFIG_PATH, "tab_items").listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json")) {
                    TabEditJsonRepresentation.load(tabEditFile, jsonTabEdits);
                }
            }
        }

        tabEdits.clear();
        jsonTabEdits.forEach((s, tab) -> {
            CreativeModeTab tabKey = CreativeTabUtils.getTabFromString(s);
            tabEdits.put(tabKey, new TabEditConfig(tab));
        });
        LOGGER.debug("Tab Edits: {}", tabEdits);

        //-------------------------------------------------------
        //Load new tabs
        //-------------------------------------------------------
//        HashMap<String, TabEditJsonRepresentation> jsonNewTabs = new HashMap<>();
//        TabEditJsonRepresentation.load(new File(CONFIG_PATH, "new_tabs.json"), jsonNewTabs);
//        subfiles = new File(CONFIG_PATH, "new_tabs").listFiles();
//        if (subfiles != null) {
//            for (File tabEditFile : subfiles) {
//                if (tabEditFile.getName().endsWith(".json")) {
//                    TabEditJsonRepresentation.load(tabEditFile, jsonNewTabs);
//                }
//            }
//        }
//
//        newTabs.clear();
//        jsonNewTabs.forEach((s, tab) -> {
//            TabEditConfig tabData = new TabEditConfig(tab);
//            CreativeModeTab newTab = CreativeTabUtils.makeNewTab(s, tabData.tab_icon);
//            newTabs.put(newTab, tabData);
//        });
//        LOGGER.debug("New tabs: {}", newTabs);


    }


    public void setWasReloaded(boolean b) {
        wasReloaded = b;
    }

    public boolean isWasReloaded() {
        return wasReloaded;
    }


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
