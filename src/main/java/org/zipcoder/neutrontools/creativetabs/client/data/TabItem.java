package org.zipcoder.neutrontools.creativetabs.client.data;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TabItem {
    @SerializedName("index") //TODO: Add support for this
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
        if (hideFromOtherTabs) CreativeTabEdits.INSTANCE.getHiddenItems().add(item);

        ItemStack stack = new ItemStack(item, 1);
        if (tag != null) {
            stack.setTag(tag);
            if (tag.contains("customName"))
                stack.setHoverName(Component.literal(tag.getString("customName")));
        }
        return stack;
    }

    private ItemStack makeStack(String name, CompoundTag tag) {
        ItemStack stack = CreativeTabUtils.makeItemStack(name);
        if (hideFromOtherTabs) CreativeTabEdits.INSTANCE.getHiddenItems().add(stack.getItem());

        if (tag != null) {
            stack.setTag(tag);
            if (tag.contains("customName"))
                stack.setHoverName(Component.literal(tag.getString("customName")));
        }
        return stack;
    }

    /**
     * Returns a list of items that match the given item match
     * The match is based on if ALL conditions are met
     * If an item with every tag specified and every name regex specified is found, it will be added to the list
     *
     * @return
     */
    public List<ItemStack> makeStacksForAdditions() {
        List<ItemStack> items = new ArrayList<>();

        //Add NBT
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
                    List<ItemStack> itemStacks = CreativeTabEdits.INSTANCE.original_tabDisplayItems.get(tab);
                    if (itemStacks != null) {
                        items.addAll(itemStacks);
                    }
                }
                return items;
            }

            List<Item> itemsForMatch = getItemsForMatch();
            itemsForMatch.forEach(i -> {
                if (NeutronTools.CONFIG.ensureNoDuplicatesBetweenTabs) {
                    //TODO: This is a hacky fix to prevent matches from creating items in multiple tabs, Duplicates may still occur, and the user may even want them, Create a better solution in the future.
                    if (CreativeTabEdits.INSTANCE.getHiddenItems().contains(i))
                        return; //Skip duplicates for matches
                }
                items.add(makeStack(i, tag));
            });
            return items;
        } else if (names != null && names.length > 0) {
            for (String name : names) {
                ItemStack stack = makeStack(name, tag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
            return items;
        } else {//If this is just a normal item
            ItemStack stack = makeStack(name, tag);
            if (!stack.isEmpty()) {
                items.add(stack);
            }
            return items;
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
            Pattern pattern = Pattern.compile(match.nameRegex);

            allItems.addAll(ForgeRegistries.ITEMS.getValues().stream().filter(item -> {
                // Get the registry name (e.g., "minecraft:iron_ore")
                String registryName = ForgeRegistries.ITEMS.getKey(item).toString();
                return pattern.matcher(registryName).matches();
            }).collect(Collectors.toList()));
        }
        return allItems;
    }

    private static List<Item> getItemsWithTags(TabItem match, List<Item> allItems) {
        if (match.match_tags == null || match.match_tags.length == 0) {
            return allItems;
        }

        var tagManager = ForgeRegistries.ITEMS.tags();

        for (String tag : match.match_tags) {
            if (ResourceLocation.isValidResourceLocation(tag)) {
                ResourceLocation location = new ResourceLocation(tag);
                TagKey<Item> tagKey = tagManager.createTagKey(location);
                ITag<Item> tagContents = tagManager.getTag(tagKey);

                if (!tagManager.isKnownTagName(tagKey)) {
                    NeutronTools.LOGGER.warn("No known tag name found for: {}", tagKey);
                    continue;
                }
                allItems.addAll(tagContents.stream().collect(Collectors.toSet()));
            } else {
                NeutronTools.LOGGER.warn("Invalid tag: {}", tag);
            }
        }
        return allItems;
    }

}