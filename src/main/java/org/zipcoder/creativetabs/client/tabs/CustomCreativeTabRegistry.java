package org.zipcoder.creativetabs.client.tabs;

import com.google.gson.Gson;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
import org.zipcoder.creativetabs.ModConstants;
import org.zipcoder.creativetabs.client.data.CustomCreativeTabJsonHelper;
import org.zipcoder.creativetabs.client.data.DisabledTabsJsonHelper;
import org.zipcoder.creativetabs.client.data.OrderedTabsJsonHelper;
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.zipcoder.creativetabs.utils.CreativeTabUtils.*;

//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
public class CustomCreativeTabRegistry {

    public static final CustomCreativeTabRegistry INSTANCE = new CustomCreativeTabRegistry();
    protected final Gson GSON = new Gson();

    private final List<CreativeModeTab> vanillaTabs = new ArrayList<>();
    private final LinkedHashSet<CreativeModeTab> customTabs = new LinkedHashSet<>();
    private final Set<String> disabledTabs = new HashSet<>();
    private final LinkedHashSet<String> tabOrder = new LinkedHashSet<>();
    private final LinkedList<CreativeModeTab> currentTabs = new LinkedList<>();
    public final CreativeTabRegistryAddon addon = new CreativeTabRegistryAddon(this);

    public LinkedList<CreativeModeTab> getCurrentTabs(){
        return currentTabs;
    }

    public LinkedHashSet<CreativeModeTab> getCustomTabs(){
        return customTabs;
    }

    public LinkedHashSet<String> getTabOrder(){
        return tabOrder;
    }

    public Set<String> getDisabledTabs(){
        return disabledTabs;
    }

    public List<CreativeModeTab> getVanillaTabs(){
        return vanillaTabs;
    }

    private final HashMap<String, Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacedTabs = new HashMap<>();

    public HashMap<String, Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> getReplacedTabs(){
        return replacedTabs;
    }

    private final HashMap<CreativeModeTab, List<ItemStack>> tabItems = new HashMap<>();
    private final Set<Item> hiddenItems = new HashSet<>();

    public HashMap<CreativeModeTab, List<ItemStack>> getTabItems(){
        return tabItems;
    }

    public Set<Item> getHiddenItems(){
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

    public void processEntries(Map<ResourceLocation, Resource> entries) {
        for (Map.Entry<ResourceLocation, Resource> entry : entries.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            ModConstants.logger.info("Processing {}", location.toString());

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
                            ModConstants.logger.error("Failed to Process NBT for Item {}", item.getName(), e);
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
                    customTabs.add(tab);
                    tabItems.put(tab, stacks);
                }
            } catch (Exception e) {
                ModConstants.logger.error("Failed to process creative tab", e);
            }
        }

        reorderTabs();
    }



    public void loadDisabledTabs(Map<ResourceLocation, Resource> entries) {
        entries.forEach((location, resource) -> {
            ModConstants.logger.info("Processing {}", location.toString());
            try (InputStream stream = resource.open()) {
                DisabledTabsJsonHelper json = new Gson().fromJson(new InputStreamReader(stream), DisabledTabsJsonHelper.class);
                disabledTabs.addAll(json.getDisabledTabs());
            } catch (Exception e) {
                ModConstants.logger.error("Failed to process disabled tabs for {}", location, e);
            }
        });
    }

    public void loadOrderedTabs(Map<ResourceLocation, Resource> resourceMap) {
        resourceMap.forEach((location, resource) -> {
            ModConstants.logger.info("Processing {}", location.toString());
            try (InputStream stream = resource.open()) {
                OrderedTabsJsonHelper tabs = new Gson().fromJson(new InputStreamReader(stream), OrderedTabsJsonHelper.class);
                tabOrder.addAll(tabs.getTabs());
            } catch (Exception e) {
                ModConstants.logger.error("Failed to process ordered tabs for {}", location, e);
            }
        });
    }

    protected void reorderTabs() {
        List<CreativeModeTab> oldTabs = new ArrayList<>();
        oldTabs.addAll(vanillaTabs);
        oldTabs.addAll(customTabs);

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
        filteredTabs.addAll(customTabs);

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

    // Just used to remove duplicate code
    private void processTab(CreativeModeTab tab, LinkedHashSet<CreativeModeTab> filteredTabs) {
        if (!disabledTabs.contains(getTabKey(((CreativeModeTabAccessor) tab).getInternalDisplayName()))) {
            filteredTabs.add(tab);
        }
    }

    /**
     * Clear all cached data for reloading
     */
    public void clearTabs() {
        wasReloaded = true;

        customTabs.clear();
        hiddenItems.clear();
        disabledTabs.clear();
        tabItems.clear();
        tabOrder.clear();
        currentTabs.clear();
        replacedTabs.clear();
        addon.clear();
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
