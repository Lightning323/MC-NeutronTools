package org.lightning.neutrontools.config.creativeTabs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;

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
    public HashMap<CreativeModeTab, TabEditConfig> tabEdits = new HashMap<>();
    private boolean buildSetup = false;



    public void load() {
        NeutronTools.LOGGER.debug("Loading Creative Tab Config");
        buildSetup = true;

        //-------------------------------------------------------
        //Load simple lists
        //-------------------------------------------------------
        disabledTabs.clear();
        tabOrder.clear();
        loadSimpleJsonLists(new File(CONFIG_PATH, "disabled_tabs.json"));
        loadSimpleJsonLists(new File(CONFIG_PATH, "ordered_tabs.json"));

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
                            if (NeutronCreativeTabs.INSTANCE.newTabs.containsKey(tabName)) {
                                myTab = NeutronCreativeTabs.INSTANCE.newTabs.get(tabName);
                            } else {
                                myTab = CreativeModeTab.builder()
                                        .title(Component.translatable("itemGroup." + MODID + "." + tabName))
                                        .icon(CreativeTabUtils.makeTabIcon(tabData.tab_icon.name, tabData.tab_icon.nbt))
                                        .build();
                                NeutronCreativeTabs.INSTANCE.newTabs.put(tabName, myTab);
                            }
                            tabEdits.put(myTab, new TabEditConfig(tabData));
                        });
            }
        }

        if (CONFIG.verboseMode) {
            LOGGER.info("Disabled tabs: {}", disabledTabs);
            LOGGER.info("Ordered tabs: {}", tabOrder);
            LOGGER.info("Tab Edits: {}", tabEdits);
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
        }
    }


}
