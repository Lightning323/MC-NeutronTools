package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
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

    public HashMap<CreativeModeTab, TabEditConfig> newTabs = new HashMap<>();
    public HashMap<CreativeModeTab, TabEditConfig> tabEdits = new HashMap<>();

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
        tabEdits.clear();
        tabOrder.clear();

        //Load tab edits
        tabEdits.loadJson(new File(CONFIG_PATH, "tab_items.json"));
        File[] subfiles = new File(CONFIG_PATH, "tab_items").listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json")) {
                    tabEdits.loadJson(tabEditFile);
                }
            }
        }

        //Load new tabs
        newTabs.loadJson(new File(CONFIG_PATH, "new_tabs.json"));
        subfiles = new File(CONFIG_PATH, "new_tabs").listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json")) {
                    newTabs.loadJson(tabEditFile);
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


    private CreativeModeTab makeNewTab(String titleKey, ItemConfig icon) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        builder.title(Component.translatable(prefix(titleKey)));
        builder.icon(makeTabIcon(icon));
        return builder.build();
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
