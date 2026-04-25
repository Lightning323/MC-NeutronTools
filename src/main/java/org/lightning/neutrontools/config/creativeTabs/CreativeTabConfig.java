package org.lightning.neutrontools.config.creativeTabs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.lightning.neutrontools.NeutronTools.*;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.makeItemStack;

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
            new File(CONFIG_PATH, "NeutronTools.tabs.newTabs").mkdirs();
            new File(CONFIG_PATH, "tab_edits").mkdirs();
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
        LOGGER.debug("Disabled items: {}", disabledItems);

        //-------------------------------------------------------
        //Load tab edits / new tabs
        //-------------------------------------------------------
        tabEdits.clear();

        File[] subfiles = new File(CONFIG_PATH, "tab_edits").listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json"))
                    TabEditJsonRepresentation.load(tabEditFile, (key, value) -> {
                        CreativeModeTab creativeTab = CreativeTabUtils.getTabFromString(key);
                        if (creativeTab != null) tabEdits.put(creativeTab, new TabEditConfig(value));
                    });
            }
        }
        subfiles = new File(CONFIG_PATH, "new_tabs").listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json")) TabEditJsonRepresentation.load(tabEditFile,
                        (tabName, tabData) -> {
                            //Make the new tab as we are indexing tab edit files
                            //Tab edits and new tabs do the same thing, we create the new tab, but then add items with tab edits
                            CreativeModeTab myTab;
                            if (NeutronTools.TABS.newTabs.containsKey(tabName)) {
                                myTab = NeutronTools.TABS.newTabs.get(tabName);
                            } else {
                                myTab = CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup." + MODID + "." + tabName))
                                        .icon(CreativeTabUtils.makeTabIcon(tabData.tab_icon.name, tabData.tab_icon.nbt))
                                        .build();
                                NeutronTools.TABS.newTabs.put(tabName, myTab);
                            }
                            tabEdits.put(myTab, new TabEditConfig(tabData));
                        });
            }
        }


        LOGGER.info("Tab Edits: {}", tabEdits);
        LOGGER.info("New tabs: {}", NeutronTools.TABS.newTabs);
    }


    public void setWasReloaded(boolean b) {
        wasReloaded = b;
    }

    public boolean isWasReloaded() {
        return wasReloaded;
    }


    public boolean isTabDisabled(CreativeModeTab self) {
        return disabledTabs.contains(CreativeTabUtils.getRegistryID(self));
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
