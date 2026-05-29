package org.lightning.neutrontools.config.creativeTabs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.NeutronConfig;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;

import static org.lightning.neutrontools.NeutronTools.*;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.makeItemStack;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
public class NeutronCreativeTabConfig {


    public void setBuildSetup(boolean b) {
        buildSetup = b;
    }

    public boolean buildSetup() {
        return buildSetup;
    }


    public NeutronCreativeTabConfig() {
        load();
        buildSetup = true;
    }

    public final Set<String> disabledTabs = new HashSet<>();


    public final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();
    private final HashMap<String, TabEditConfig> tabEdits = new HashMap<>();
    private final HashMap<CreativeModeTab, TabEditConfig> newTabEdits = new HashMap<>();
    private boolean buildSetup = false;

    /**
     * Get the tab edit for a given tab OR a new tab edit if the tab does not exist
     * @param tab
     * @return
     */
    public TabEditConfig getTabEdit(CreativeModeTab tab) {
        TabEditConfig out = tabEdits.get(CreativeTabUtils.getRegistryID(tab));
        if (out == null) {
            out = newTabEdits.get(tab);
        }
        return out;
    }

    public TabEditConfig getTabEdit(ResourceLocation tabId) {
        TabEditConfig out = tabEdits.get(tabId.toString());
        return out;
    }

    public TabEditConfig getTabEdit(String tabId) {
        TabEditConfig out = tabEdits.get(tabId);
        return out;
    }


    public void load() {
        NeutronTools.LOG.debug("Loading Creative Tab Config");
        buildSetup = true;

        //-------------------------------------------------------
        //Load simple lists
        //-------------------------------------------------------
        disabledTabs.clear();
        tabOrder.clear();
        loadSimpleJsonLists(NeutronConfig.DISABLED_TABS_FILE);
        loadSimpleJsonLists(NeutronConfig.ORDERED_TABS_FILE);

        //-------------------------------------------------------
        //Load tab edits / new tabs
        //-------------------------------------------------------
        tabEdits.clear();
        File[] subfiles = NeutronConfig.TAB_EDITS_DIRECTORY.listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json"))
                    TabEditJsonRepresentation.load(tabEditFile, (key, value) -> {
                        tabEdits.put(key, new TabEditConfig(value));
                    });
            }
        }
        subfiles = NeutronConfig.NEW_TABS_DIRECTORY.listFiles();
        if (subfiles != null) {
            for (File tabEditFile : subfiles) {
                if (tabEditFile.getName().endsWith(".json")) TabEditJsonRepresentation.load(tabEditFile,
                        (key, tabData) -> {
                            //Make the new tab as we are indexing tab edit files
                            //Tab edits and new tabs do the same thing, we create the new tab, but then add items with tab edits
                            CreativeModeTab myTab;
                            if (NeutronCreativeTabs.INSTANCE.newTabs.containsKey(key)) {
                                myTab = NeutronCreativeTabs.INSTANCE.newTabs.get(key);
                            } else {
                                myTab = CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup." + MODID + "." + key))
                                        .icon(CreativeTabUtils.makeTabIcon(tabData.tab_icon.name, tabData.tab_icon.nbt))
                                        .build();
                                NeutronCreativeTabs.INSTANCE.newTabs.put(key, myTab);
                            }
                            newTabEdits.put(myTab, new TabEditConfig(tabData));
                            tabEdits.put(key, new TabEditConfig(tabData));
                        });
            }
        }

        if (CONFIG.verboseMode) {
            LOG.info("Disabled tabs: {}", disabledTabs);
            LOG.info("Ordered tabs: {}", tabOrder);
            LOG.info("Tab Edits: {}", tabEdits);
//        LOGGER.info("New tabs: {}", NeutronTools.TABS.newTabs);
        }
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
            NeutronTools.LOG.warn("Failed to parse config file: {}", file);
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
        }
    }


}
