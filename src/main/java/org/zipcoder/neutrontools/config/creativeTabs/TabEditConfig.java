package org.zipcoder.neutrontools.config.creativeTabs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TabEditConfig {

    public Supplier<ItemStack> tab_icon;
    public String tab_name_key;
    public Map<Integer, List<ItemStack>> items_to_add;
    public ArrayList<Item> items_to_remove;


    public void load(TabEditJsonRepresentation json) {
        this.tab_name_key = json.tab_name_key;
        this.tab_icon = CreativeTabUtils.makeTabIcon(json.tab_icon.name, json.tab_icon.nbt);
        items_to_add = new HashMap<>();
        items_to_remove = new ArrayList<>();

        json.items_to_add.forEach(item -> {

            int index = item.index;

            ArrayList<ItemStack> stacks = new ArrayList<>();

            if (item.names != null) {
                for (String name : item.names) {
                    stacks.add(CreativeTabUtils.makeItemStack(name, item.nbt));
                }
            }

            if (item.match_name != null) {

            }
            if (item.match_tab != null) {

            }
            if (item.match_tags != null) {

            }


            CompoundTag tagt = null;
            if (tabItemList.nbt != null) {
                if (!tabItemList.nbt.isEmpty()) {
                    try {
                        tagt = TagParser.parseTag(tabItemList.nbt);
                    } catch (CommandSyntaxException e) {
                        NeutronTools.LOGGER.error("Failed to Process NBT for Item {}", tabItemList.name, e);
                    }
                }
            }
            final CompoundTag tag = tagt; //Our NBT data

            if (tabItemList.isMatch()) {
                if (tabItemList.match_tab != null && !tabItemList.match_tab.isEmpty()) {
                    CreativeModeTab tab = CreativeTabUtils.getTabFromString(tabItemList.match_tab);
                    if (tab == null) {
                        NeutronTools.LOGGER.warn("Failed to find tab for {}", tabItemList.match_tab);
                    }
                }

                List<Item> itemsForMatch = tabItemList.getItemsForMatch();
                List<ItemStack> stacks = new ArrayList<>();
                itemsForMatch.forEach(i -> {//We dont want to reintroduce hidden items
                    if (!CreativeTabConfig.INSTANCE.disabledItems.contains(i))
                        stacks.add(tabItemList.makeStack(i, tag));
                });
                additionList.addStacks(tabItemList.index, stacks);

            } else if (tabItemList.names != null && tabItemList.names.length > 0) {
                List<ItemStack> stacks = new ArrayList<>();
                for (String name : tabItemList.names) {
                    stacks.add(tabItemList.makeStack(name, tag));
                }
                additionList.addStacks(tabItemList.index, stacks);
            } else {//If this is just a normal item
                additionList.addStack(tabItemList.index, tabItemList.makeStack(tabItemList.name, tag));
            }

        });
    }


    /**
     * Inserts the stored items into the provided collection at their
     * respective index positions.
     */
    public void addItemsInto(Collection<ItemStack> inputStacks) {
        if (inputStacks == null || itemMap.isEmpty()) return;

        // If the collection is a List, we can use positional access
        if (inputStacks instanceof List) {
            List<ItemStack> list = (List<ItemStack>) inputStacks;

            // Iterate through our map of additions
            for (Map.Entry<Integer, List<ItemStack>> entry : itemMap.entrySet()) {
                int index = entry.getKey();
                List<ItemStack> itemsToAdd = entry.getValue();

                //Safety layer to prevent items that dont have proper stack size
                itemsToAdd.removeIf(stack -> stack.getCount() != 1);


                if (list.isEmpty()) {//add relative to beginning
                    list.addAll(itemsToAdd);
                } else if (index >= 0 && index < list.size()) {//add relative to beginning
                    list.addAll(index, itemsToAdd);
                } else if (index == list.size() || index == -1) { //dd to the end
                    list.addAll(itemsToAdd);
                } else if (index < 0) { //add to the end
                    list.addAll(list.size() + index + 1, itemsToAdd);
                }
            }
        } else {
            // If it's just a general collection (like a Set),
            // positional insertion isn't strictly possible, so we just add them.
            itemMap.values().forEach(inputStacks::addAll);
        }
    }


    public boolean isMatch() {
        return (nameRegex != null && !nameRegex.isBlank()) ||
                (match_tags != null && match_tags.length > 0) ||
                (match_tab != null && !match_tab.isBlank());
    }


    /**
     * Helper to bridge the gap between legacy NBT and 1.21 Data Components
     */
    private void applyLegacyTag(ItemStack stack, CompoundTag tag) {
        if (tag == null) return;

        // 1. Handle Custom Name (Specialized Component)
        if (tag.contains("customName")) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(tag.getString("customName")));
            // Remove from tag so it doesn't duplicate into the custom_data component
            tag.remove("customName");
        }

        // 2. Handle all other NBT data
        // In 1.21, arbitrary NBT is stored in the "custom_data" component
        if (!tag.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }


    private static List<Item> addByNameRegex(TabItemConfig match, List<Item> allItems) {
        if (match.nameRegex != null && !match.nameRegex.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(match.nameRegex);
                var registry = BuiltInRegistries.ITEM;

                // Stream through the registry entries
                List<Item> matchedItems = registry.entrySet().stream()
                        .filter(entry -> {
                            // entry.getKey().location() returns the ResourceLocation
                            String registryName = entry.getKey().location().toString();
                            return pattern.matcher(registryName).find(); // Use .find() or .matches() depending on intent
                        })
                        .map(java.util.Map.Entry::getValue)
                        .toList();

                allItems.addAll(matchedItems);
            } catch (PatternSyntaxException e) {
                NeutronTools.LOGGER.error("Invalid regex pattern in config: {}", match.nameRegex);
            }
        }
        return allItems;
    }

    private static List<Item> getItemsWithTags(TabItemConfig match, List<Item> allItems) {
        if (match.match_tags == null || match.match_tags.length == 0) {
            return allItems;
        }

        // In 1.21.1, we use BuiltInRegistries for standard access
        var registry = BuiltInRegistries.ITEM;

        for (String tag : match.match_tags) {
            // ResourceLocation constructor is deprecated/removed; use parse or tryParse
            ResourceLocation location = ResourceLocation.tryParse(tag);

            if (location != null) {
                // Create the TagKey using the Item Registry Key
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, location);

                // Fetch the tag holder from the registry
                var tagOptional = registry.getTag(tagKey);

                if (tagOptional.isPresent()) {
                    // Stream the contents of the tag into the list
                    tagOptional.get().forEach(holder -> allItems.add(holder.value()));
                } else {
                    NeutronTools.LOGGER.warn("No known tag name found for: {}", tagKey);
                }
            } else {
                NeutronTools.LOGGER.warn("Invalid tag format: {}", tag);
            }
        }
        return allItems;
    }


}