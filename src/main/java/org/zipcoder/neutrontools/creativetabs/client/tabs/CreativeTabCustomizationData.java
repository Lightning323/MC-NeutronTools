package org.zipcoder.neutrontools.creativetabs.client.tabs;

import com.google.gson.Gson;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraftforge.fml.loading.FMLPaths;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.client.data.*;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.zipcoder.neutrontools.utils.CreativeTabUtils.*;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
public class CreativeTabCustomizationData {

    public static final CreativeTabCustomizationData INSTANCE = new CreativeTabCustomizationData();
    protected final Gson GSON = new Gson();

    private final List<CreativeModeTab> vanillaTabs = new ArrayList<>();
    private final LinkedHashSet<CreativeModeTab> newTabs = new LinkedHashSet<>();
    public final Set<String> disabledTabs = new HashSet<>();
    public final Set<String> disabledItems = new HashSet<>();
    private final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();
    private final LinkedList<CreativeModeTab> currentTabs = new LinkedList<>();
    public final HashMap<String, List<ItemStack>> itemsToAdd = new HashMap<>();
    public final HashMap<String, List<String>> itemsToDelete = new HashMap<>();
    private final HashMap<CreativeModeTab, List<ItemStack>> tabItems = new HashMap<>();
    private final Set<Item> hiddenItems = new HashSet<>();

    /**
     * Clear all cached data for reloading
     */
    public void clearTabs() {
        NeutronTools.LOGGER.debug("Clearing tab Data");
        wasReloaded = true;

        newTabs.clear();
        hiddenItems.clear();
        disabledTabs.clear();
        disabledItems.clear();
        tabItems.clear();
        tabOrder.clear();
        currentTabs.clear();
        replacedTabs.clear();
        itemsToAdd.clear();
        itemsToDelete.clear();
    }

    public ItemStack makeStack(CustomCreativeTabJsonHelper.TabItem item) {
        ItemStack stack = getItemStack(item.getName());
        if (stack.isEmpty()) return stack;

        if (item.getNbt() != null && !item.getNbt().isEmpty()) {
            try {
                CompoundTag tag = TagParser.parseTag(item.getNbt());
                stack.setTag(tag);

                if (tag.contains("customName"))
                    stack.setHoverName(Component.literal(tag.getString("customName")));
            } catch (CommandSyntaxException e) {
                NeutronTools.LOGGER.error("Failed to Process NBT for Item {}", item.getName(), e);
            }
        }
        return stack;
    }

    public void loadItemsForTabs(Map<ResourceLocation, Resource> itemsJson) {
        if (!itemsJson.isEmpty()) {
            for (Map.Entry<ResourceLocation, Resource> entry : itemsJson.entrySet()) {
                ResourceLocation location = entry.getKey();
                Resource resource = entry.getValue();

                NeutronTools.LOGGER.info("Processing tab data {}", location.toString());

                //Iterate over each resource (JSON file)
                try (InputStream stream = resource.open()) {
                    ItemTabJsonHelper json = GSON.fromJson(new InputStreamReader(stream), ItemTabJsonHelper.class);

                    //Iterate over each tab in the json file
                    json.getTabs().forEach(tab -> {
                        String tabName = tab.tabName;

                        //Add new tab entries if they don't exist
                        if (itemsToAdd.get(tabName) == null) {
                            itemsToAdd.put(tabName, new ArrayList<>());
                        }
                        if (itemsToDelete.get(tabName) == null) {
                            itemsToDelete.put(tabName, new ArrayList<>());
                        }

                        //Add the items to the final entry
                        for (int i = 0; i < tab.itemsAdd.length; i++) {
                            ItemStack stack = makeStack(tab.itemsAdd[i]);
                            //Only add item stacks that actually exist
                            if (!stack.isEmpty()) itemsToAdd.get(tabName).add(stack);
                        }

                        for (int i = 0; i < tab.itemsRemove.length; i++) {
                            itemsToDelete.get(tabName).add(tab.itemsRemove[i]);
                        }

                    });

                } catch (Exception e) {
                    NeutronTools.LOGGER.error("Failed to process items in creative tab", e);
                }
            }
        }
    }

    public LinkedList<CreativeModeTab> getCurrentTabs() {
        return currentTabs;
    }

    public LinkedHashSet<CreativeModeTab> getNewTabs() {
        return newTabs;
    }

    private final HashMap<String, Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacedTabs = new HashMap<>();

    public HashMap<String, Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> getReplacedTabs() {
        return replacedTabs;
    }


    public HashMap<CreativeModeTab, List<ItemStack>> getTabItems() {
        return tabItems;
    }

    public Set<Item> getHiddenItems() {
        return hiddenItems;
    }

    //    @Setter
    private boolean showTabNames = false;

    public void setShowTabNames(boolean showTabNames) {
        this.showTabNames = showTabNames;
    }

    public boolean isShowTabNames() {
        return showTabNames;
    }


    //    @Setter
    private boolean wasReloaded = false;

    public void setWasReloaded(boolean b) {
        wasReloaded = b;
    }

    public boolean isWasReloaded() {
        return wasReloaded;
    }


    private final CreativeModeTab OP_TAB = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getOpBlockTab());

    public void loadCustomTabs(Map<ResourceLocation, Resource> entries) {
        for (Map.Entry<ResourceLocation, Resource> entry : entries.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            NeutronTools.LOGGER.info("Processing tab data {}", location.toString());

            try (InputStream stream = resource.open()) {
                CustomCreativeTabJsonHelper json = GSON.fromJson(new InputStreamReader(stream), CustomCreativeTabJsonHelper.class);
                ArrayList<ItemStack> stacks = new ArrayList<>();

                if (!json.isTabEnabled())
                    continue;

                for (CustomCreativeTabJsonHelper.TabItem item : json.getTabItems()) {
                    if (item.getName().equalsIgnoreCase("existing"))
                        json.setKeepExisting(true);

                    ItemStack stack = getItemStack(item.getName());
                    if (stack.isEmpty())
                        continue;

                    if (item.isHideOldTab())
                        hiddenItems.add(stack.getItem());

                    if (item.getNbt() != null && !item.getNbt().isEmpty()) {
                        try {
                            CompoundTag tag = TagParser.parseTag(item.getNbt());
                            stack.setTag(tag);

                            if (tag.contains("customName"))
                                stack.setHoverName(Component.literal(tag.getString("customName")));
                        } catch (CommandSyntaxException e) {
                            NeutronTools.LOGGER.error("Failed to Process NBT for Item {}", item.getName(), e);
                        }
                    }

                    stacks.add(stack);
                }

                if (json.isReplace()) {
                    replacedTabs.put(fileToTab(location.getPath()).toLowerCase(), Pair.of(json, stacks));
                } else {
                    CreativeModeTab.Builder builder = new CreativeModeTab.Builder(null, -1);
                    builder.title(Component.translatable(prefix(json.getTabName())));
                    builder.icon(makeTabIcon(json));

                    if (json.getTabBackground() != null && !json.getTabBackground().isEmpty())
                        builder.backgroundSuffix(json.getTabBackground());

                    CreativeModeTab tab = builder.build();
                    newTabs.add(tab);
                    tabItems.put(tab, stacks);
                }
            } catch (Exception e) {
                NeutronTools.LOGGER.error("Failed to process creative tab", e);
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
                    NeutronTools.LOGGER.error("Failed to process disabled tabs for {}", location, e);
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
                    disabledItems.addAll(json.getDisabledItems());
                } catch (Exception e) {
                    NeutronTools.LOGGER.error("Failed to process disabled items for {}", location, e);
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
                            disabledItems.add(line.strip());
                        }
                    });
                } catch (IOException e) {
                    NeutronTools.LOGGER.error("Failed to process JEI blacklisted items {}", e);
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
                    tabOrder.addAll(tabs.getTabs());
                } catch (Exception e) {
                    NeutronTools.LOGGER.error("Failed to process ordered tabs for {}", location, e);
                }
            });
        }
    }

    public void reorderTabs() {
        List<CreativeModeTab> oldTabs = new ArrayList<>();
        oldTabs.addAll(vanillaTabs);
        oldTabs.addAll(newTabs);

        LinkedHashSet<CreativeModeTab> filteredTabs = new LinkedHashSet<>();
        boolean addExisting = false;

        if (!tabOrder.isEmpty()) {
            for (String orderedTab : tabOrder) {
                if (!orderedTab.equalsIgnoreCase("existing")) {
                    oldTabs.stream()
                            .filter(tab -> getTabKey(((CreativeModeTabAccessor) tab).getInternalDisplayName()).equals(orderedTab))
                            .findFirst().ifPresent(pTab -> processTab(pTab, filteredTabs));
                } else {
                    addExisting = true;
                }
            }
        } else {
            addExisting = true;
        }


        if (addExisting) {
            for (CreativeModeTab tab : oldTabs) {
                processTab(tab, filteredTabs);
            }
        }

        // Don't disable the Survival Inventory, Search and Hotbar
        filteredTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab()));
        filteredTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getHotbarTab()));
        filteredTabs.add(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getInventoryTab()));

        // Don't disable Custom Tabs
        filteredTabs.addAll(newTabs);

        CreativeModeTabAccessor searchTab = (CreativeModeTabAccessor) BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabsAccessor.getSearchTab());
        searchTab.setDisplayItemsGenerator((itemDisplayParameters, output) -> {
            Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndTagSet();

            for (CreativeModeTab tab : getCurrentTabs()) {
                if (tab.getType() == CreativeModeTab.Type.SEARCH)
                    continue;

                stacks.addAll(tab.getSearchTabDisplayItems());
            }

            output.acceptAll(stacks);
        });

        currentTabs.clear();
        currentTabs.addAll(filteredTabs.stream().toList());

        CreativeModeTabs.validate();
    }

    private void processTab(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        String tabName = getTabKey(((CreativeModeTabAccessor) tab).getInternalDisplayName());
        NeutronTools.LOGGER.debug("Processing tab: {}", tabName);
        if (!disabledTabs.contains(tabName)) {
            filteredTabs.add(tab);
        }
    }


    public List<CreativeModeTab> sortedTabs() {
        return this.currentTabs;
    }

    public List<CreativeModeTab> displayedTabs() {
        return this.currentTabs.stream().filter(t -> {
            if (t == OP_TAB && !Minecraft.getInstance().options.operatorItemsTab().get())
                return false;

            return t.shouldDisplay();
        }).toList();
    }

    public void setVanillaTabs(List<CreativeModeTab> tabs) {
        this.vanillaTabs.clear();
        this.vanillaTabs.addAll(tabs);
    }

}
