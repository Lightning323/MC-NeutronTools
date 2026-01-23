package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.Gson;
import net.minecraftforge.fml.loading.FMLPaths;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.*;
import org.apache.commons.lang3.tuple.Pair;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.ibm.icu.util.LocalePriorityList.add;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.*;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
public class CreativeTabEdits {

    public static final CreativeTabEdits INSTANCE = new CreativeTabEdits();
    protected final Gson GSON = new Gson();

    public final static List<CreativeModeTab> mandatoryTabs = new ArrayList<>();
    static {
        mandatoryTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab()));
        mandatoryTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getHotbarTab()));
        mandatoryTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getInventoryTab()));
    }

    private final List<CreativeModeTab> vanillaTabs = new ArrayList<>();
    public final LinkedList<CreativeModeTab> sortedTabs = new LinkedList<>();
    private final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();

    public final LinkedHashSet<CreativeModeTab> newTabs = new LinkedHashSet<>();
    public final HashMap<CreativeModeTab, ItemAdditionList> tabAdditions = new HashMap<>();
    public final HashMap<CreativeModeTab, Set<Item>> tabRemovals = new HashMap<>();
    public final Set<String> disabledTabs = new HashSet<>();
    private final Set<Item> hiddenItems = new HashSet<>();
//    private boolean enabled = true; //TODO: I dont want to allow this feature until I feel it is free from potential crashes
    private boolean wasReloadedFirstTime = false;
    private boolean wasReloaded = false;

    //For caching the original state of the creative tabs
    public List<CreativeModeTab> original_SortedTabs;
    public final HashMap<CreativeModeTab, List<ItemStack>> original_tabDisplayItems = new HashMap<>();

    public boolean isEnabled() {
        return wasReloadedFirstTime;
    }
    public void setWasReloaded(boolean b) {
        wasReloaded = b;
    }
    public boolean isWasReloaded() {
        return wasReloaded;
    }
    public boolean isWasReloadedFirstTime() {
        return wasReloadedFirstTime;
    }

    /**
     * Clear all cached data for reloading
     */
    public void clearTabs() {
        NeutronTools.LOGGER.debug("Clearing tab Data");
        wasReloaded = true;
        wasReloadedFirstTime = true;

        newTabs.clear();
        hiddenItems.clear();
        disabledTabs.clear();
        tabAdditions.clear();
        tabOrder.clear();
        sortedTabs.clear();
        replacedTabs.clear();
        tabRemovals.clear();
    }


    public void loadItemsForTabs(Map<ResourceLocation, Resource> itemsJson) {
        if (!itemsJson.isEmpty()) {
            for (Map.Entry<ResourceLocation, Resource> entry : itemsJson.entrySet()) {
                ResourceLocation location = entry.getKey();
                Resource resource = entry.getValue();

                NeutronTools.LOGGER.info("Processing tab data {}", location.toString());

                //Iterate over each resource (JSON file)
                try (InputStream stream = resource.open()) {
                    //Iterate over each tab in the json file
                    GSON.fromJson(new InputStreamReader(stream), TabItemsJsonHelper.class)
                            .getTabs().forEach(json -> {
                                CreativeModeTab tab = CreativeTabUtils.getTabFromString(json.tabName);

                                if (tab != null) {
                                    //Add new tab entries if they don't exist
                                    tabAdditions.computeIfAbsent(tab, k -> new ItemAdditionList());
                                    tabRemovals.computeIfAbsent(tab, k -> new HashSet<>());

                                    ItemAdditionList thisTabAdditions = tabAdditions.get(tab);
                                    for (int i = 0; i < json.itemsAdd.length; i++) {
                                        TabItem tabItem = json.itemsAdd[i];
                                        tabItem.populateAdditions(thisTabAdditions);
                                    }
                                    Set<Item> thisTabDeletions = tabRemovals.get(tab);
                                    for (int i = 0; i < json.itemsRemove.length; i++) {
                                        TabItem tabItem = json.itemsRemove[i];
                                        thisTabDeletions.addAll(tabItem.makeItemsForRemoval());
                                    }

                                }

                            });
                } catch (Exception e) {
                    NeutronTools.LOGGER.warn("Failed to process items in creative tab", e);
                }
            }
        }
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


    public Set<Item> getHiddenItems() {
        return hiddenItems;
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


    private final CreativeModeTab OP_TAB = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getOpBlockTab());

    public void loadNewTabs(Map<ResourceLocation, Resource> entries) {
        for (Map.Entry<ResourceLocation, Resource> entry : entries.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            NeutronTools.LOGGER.info("Processing tab data {}", location.toString());

            try (InputStream stream = resource.open()) {
                NewTabJsonHelper json = GSON.fromJson(new InputStreamReader(stream), NewTabJsonHelper.class);
                ItemAdditionList additionList = new ItemAdditionList();

                if (!json.isTabEnabled())
                    continue;

                for (TabItem item : json.getTabItems()) {
                    if (item.name != null && item.name.equalsIgnoreCase("existing"))
                        json.setKeepExisting(true);

                    item.populateAdditions(additionList);
                }


                if (json.replaceTab != null && !json.replaceTab.isBlank()) {
                    NeutronTools.LOGGER.info("Replaced Tab {} with {}", json.getTabName(), json.replaceTab);
                    replacedTabs.put(json.replaceTab, Pair.of(json, additionList));
                } else {
                    CreativeModeTab.Builder builder = new CreativeModeTab.Builder(null, -1);

                    builder.title(Component.translatable(prefix(json.getTabName())));
                    builder.icon(makeTabIcon(json));

                    if (json.getTabBackground() != null && !json.getTabBackground().isEmpty())
                        builder.backgroundSuffix(json.getTabBackground());

                    CreativeModeTab tab = builder.build();
                    newTabs.add(tab);
                    System.out.println("LOAD ADDITION LIST FOR TAB "+CreativeTabUtils.getTranslationKey(tab)+" _____________ "+additionList.size());
                    tabAdditions.put(tab, additionList);
                }
            } catch (Exception e) {
                NeutronTools.LOGGER.warn("Failed to process creative tab", e);
            }
        }
    }


    public void loadDisabledTabs(Map<ResourceLocation, Resource> disabledTabsJson) {
        if (!disabledTabsJson.isEmpty()) {
            disabledTabsJson.forEach((location, resource) -> {
                NeutronTools.LOGGER.info("Processing tab data {}", location.toString());
                try (InputStream stream = resource.open()) {//Process each resource
                    DisabledTabsJsonHelper json = new Gson().fromJson(new InputStreamReader(stream), DisabledTabsJsonHelper.class);
                    disabledTabs.addAll(json.getDisabledTabs());
                } catch (Exception e) {
                    NeutronTools.LOGGER.warn("Failed to process disabled tabs for {}", location, e);
                }
            });
        }
    }

    public void loadDisabledItems(Map<ResourceLocation, Resource> jsonEntries) {
        //Check in resource pack
        if (!jsonEntries.isEmpty()) {
            jsonEntries.forEach((location, resource) -> {
                NeutronTools.LOGGER.info("Processing tab data {}", location.toString());
                try (InputStream stream = resource.open()) {//Process each resource
                    DisabledItemsJsonHelper json = new Gson().fromJson(new InputStreamReader(stream), DisabledItemsJsonHelper.class);

                    json.getDisabledItems().forEach(e -> {
                        hiddenItems.add(makeItemStack(e).getItem());
                    });

                } catch (Exception e) {
                    NeutronTools.LOGGER.warn("Failed to process disabled items for {}", location, e);
                }
            });
        }
        //Check the JEI blacklist
        if (NeutronTools.CONFIG.hideCreativeTabItemsFromJEIBlacklist) {
            Path configDir = FMLPaths.CONFIGDIR.get();
            File jeiBlacklist = new File(configDir.toFile(), "jei/blacklist.cfg");
            if (jeiBlacklist.exists()) {
                try {
                    Files.readAllLines(jeiBlacklist.toPath()).forEach(line -> {
                        if (!line.isBlank()) {
                            hiddenItems.add(makeItemStack(line.strip()).getItem());
                        }
                    });
                } catch (IOException e) {
                    NeutronTools.LOGGER.warn("Failed to process JEI blacklisted items {}", e);
                }
            }
        }
    }

    public void loadOrderedTabs(Map<ResourceLocation, Resource> resourceMap) {
        if (!resourceMap.isEmpty()) {
            resourceMap.forEach((location, resource) -> {
                NeutronTools.LOGGER.info("Processing tab data {}", location.toString());
                try (InputStream stream = resource.open()) {
                    OrderedTabsJsonHelper tabs = new Gson().fromJson(new InputStreamReader(stream), OrderedTabsJsonHelper.class);
                    tabOrder.addAll(tabs.tabs);
                } catch (Exception e) {
                    NeutronTools.LOGGER.warn("Failed to process ordered tabs for {}", location, e);
                }
            });
        }
    }

    public void reorderTabs() {
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

//        sortedTabs.forEach(tab -> NeutronTools.LOGGER.debug("\tORDERED Tab: {}", tab.getDisplayName().getString()));
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
