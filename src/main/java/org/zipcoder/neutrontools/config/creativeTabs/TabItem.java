package org.zipcoder.neutrontools.config.creativeTabs;

import com.google.gson.annotations.SerializedName;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class TabItem {
    @SerializedName("index")
    public int index = -1;

    @SerializedName("name")
    public String name;

    @SerializedName("names")
    public String[] names;

    @SerializedName("hide_old_tab")
    public boolean hideFromOtherTabs = false;

    @SerializedName("nbt")
    public String nbt;

    //For matching items
    @SerializedName("match_name")
    public String nameRegex;

    @SerializedName("match_tags")
    public String[] match_tags;

    @SerializedName("match_tab")
    public String match_tab;


    public boolean isMatch() {
        return (nameRegex != null && !nameRegex.isBlank()) ||
                (match_tags != null && match_tags.length > 0) ||
                (match_tab != null && !match_tab.isBlank());
    }

    private ItemStack makeStack(Item item, CompoundTag tag) {
        if (hideFromOtherTabs) CreativeTabConfig.INSTANCE.hiddenItems.add(item);

        ItemStack stack = new ItemStack(item, 1);
        applyLegacyTag(stack, tag);
        return stack;
    }

    private ItemStack makeStack(String name, CompoundTag tag) {
        ItemStack stack = CreativeTabUtils.makeItemStack(name);
        if (hideFromOtherTabs) CreativeTabConfig.INSTANCE.hiddenItems.add(stack.getItem());

        applyLegacyTag(stack, tag);
        return stack;
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
    /**
     * Returns a list of items that match the given item match
     * The match is based on if ALL conditions are met
     * If an item with every tag specified and every name regex specified is found, it will be added to the list
     *
     * @return
     */
    public void populateAdditions(ItemAdditionList additionList) {
        CompoundTag tagt = null;
        if (nbt != null) {
            if (!nbt.isEmpty()) {
                try {
                    tagt = TagParser.parseTag(nbt);
                } catch (CommandSyntaxException e) {
                    NeutronTools.LOGGER.error("Failed to Process NBT for Item {}", name, e);
                }
            }
        }
        final CompoundTag tag = tagt; //Our NBT data

        if (isMatch()) {
            if (match_tab != null && !match_tab.isEmpty()) {
                CreativeModeTab tab = CreativeTabUtils.getTabFromString(match_tab);
                if (tab == null) {
                    NeutronTools.LOGGER.warn("Failed to find tab for {}", match_tab);
                } else {
                    List<ItemStack> itemStacks = new ArrayList<>(CreativeTabConfig.INSTANCE.original_tabDisplayItems.get(tab));
                    if (itemStacks != null) {
                        itemStacks.removeIf((stack) -> CreativeTabConfig.INSTANCE.hiddenItems.contains(stack.getItem()));
                    }
                    additionList.addStacks(index, itemStacks);
                }
            }

            List<Item> itemsForMatch = getItemsForMatch();
            List<ItemStack> stacks = new ArrayList<>();
            itemsForMatch.forEach(i -> {//We dont want to reintroduce hidden items
                if (!CreativeTabConfig.INSTANCE.hiddenItems.contains(i)) stacks.add(makeStack(i, tag));
            });
            additionList.addStacks(index, stacks);

        } else if (names != null && names.length > 0) {
            List<ItemStack> stacks = new ArrayList<>();
            for (String name : names) {
                stacks.add(makeStack(name, tag));
            }
            additionList.addStacks(index, stacks);
        } else {//If this is just a normal item
            additionList.addStack(index, makeStack(name, tag));
        }
    }

    public Set<Item> makeItemsForRemoval() {
        Set<Item> items = new HashSet<>();
        if (isMatch()) {
            items.addAll(getItemsForMatch());
        } else {//If this is just a normal item
            Item item = CreativeTabUtils.getItemByName(name);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private List<Item> getItemsForMatch() {
        List<Item> tagMatches = getItemsWithTags(this, new ArrayList<>());
        List<Item> regexMatches = addByNameRegex(this, new ArrayList<>());

        // 3. Determine the intersection
        if (match_tags != null && match_tags.length > 0 && nameRegex != null) {
            // If BOTH are provided, intersect them
            tagMatches.retainAll(regexMatches);
            return tagMatches;
        } else if (nameRegex != null) {// Only Regex was provided
            return regexMatches;
        } else {// Only Tags were provided (or nothing)
            return tagMatches;
        }
    }


    private static List<Item> addByNameRegex(TabItem match, List<Item> allItems) {
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

    private static List<Item> getItemsWithTags(TabItem match, List<Item> allItems) {
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