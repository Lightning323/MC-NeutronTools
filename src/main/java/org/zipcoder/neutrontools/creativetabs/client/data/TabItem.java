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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TabItem {
    @SerializedName("name")
    public String name;

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
                CreativeModeTab tabFromString = CreativeTabUtils.getTabFromString(match_tab);
                if (tabFromString == null) {
                    NeutronTools.LOGGER.warn("Failed to find tab for {}", match_tab);
                } else {
                    items.addAll(tabFromString.getDisplayItems());
                }
            }

            Set<Item> tagMatches = addItemsContainingAllTags(this, new HashSet<>());
            Set<Item> regexMatches = addByNameRegex(this, new HashSet<>());

            // 3. Determine the intersection
            if (match_tags != null && match_tags.length > 0 && nameRegex != null) {
                // If BOTH are provided, intersect them
                tagMatches.retainAll(regexMatches);
                tagMatches.forEach(i -> {
                    if (NeutronTools.CONFIG.ensureNoDuplicatesBetweenTabs) {
                        //TODO: This is a hacky fix to prevent matches from creating items in multiple tabs, Duplicates may still occur, and the user may even want them, Create a better solution in the future.
                        if (CreativeTabEdits.INSTANCE.getHiddenItems().contains(i))
                            return; //Skip duplicates for matches
                    }
                    items.add(makeStack(i, tag));
                });
            } else if (nameRegex != null) {// Only Regex was provided
                regexMatches.forEach(i -> {
                    if (NeutronTools.CONFIG.ensureNoDuplicatesBetweenTabs) {
                        if (CreativeTabEdits.INSTANCE.getHiddenItems().contains(i))
                            return; //Skip duplicates for matches
                    }
                    items.add(makeStack(i, tag));
                });
            } else {// Only Tags were provided (or nothing)
                tagMatches.forEach(i -> {
                    if (NeutronTools.CONFIG.ensureNoDuplicatesBetweenTabs) {
                        if (CreativeTabEdits.INSTANCE.getHiddenItems().contains(i))
                            return; //Skip duplicates for matches
                    }
                    items.add(makeStack(i, tag));
                });
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
        if (isMatch()) {
            if (match_tab != null && !match_tab.isEmpty()) {
                Set<Item> items = new HashSet<>();
                CreativeModeTab tabFromString = CreativeTabUtils.getTabFromString(match_tab);
                if (tabFromString == null) {
                    NeutronTools.LOGGER.warn("Failed to find tab for {}", match_tab);
                } else {
                    items.addAll(tabFromString.getDisplayItems().stream().map(ItemStack::getItem).collect(Collectors.toSet()));
                }
                return items;
            }
            Set<Item> tagMatches = addItemsContainingAllTags(this, new HashSet<>());
            Set<Item> regexMatches = addByNameRegex(this, new HashSet<>());

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
        } else {//If this is just a normal item
            Set<Item> items = new HashSet<>();
            Item item = CreativeTabUtils.getItemByName(name);
            if (item != null) {
                items.add(item);
            }
            return items;
        }
    }


    private static Set<Item> addByNameRegex(TabItem match, Set<Item> allItems) {
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

    private static Set<Item> addItemsContainingAllTags(TabItem match, Set<Item> allItems) {
        if (match.match_tags == null || match.match_tags.length == 0) {
            return allItems;
        }

        var tagManager = ForgeRegistries.ITEMS.tags();
        Set<Item> intersectingItems = null;

        for (String tag : match.match_tags) {
            if (ResourceLocation.isValidResourceLocation(tag)) {
                ResourceLocation location = new ResourceLocation(tag);
                TagKey<Item> tagKey = tagManager.createTagKey(location);
                ITag<Item> tagContents = tagManager.getTag(tagKey);

                if (!tagManager.isKnownTagName(tagKey)) {
                    NeutronTools.LOGGER.error("No known tag name found for: {}", tagKey);
                    // If one tag in the list doesn't exist, the intersection is empty
                    return allItems;
                }

                // Convert current tag contents to a temporary set for comparison
                Set<Item> currentTagSet = tagContents.stream().collect(Collectors.toSet());

                if (intersectingItems == null) {
                    // First tag: this is our starting point
                    intersectingItems = currentTagSet;
                } else {
                    // Subsequent tags: only keep items that exist in BOTH sets
                    intersectingItems.retainAll(currentTagSet);
                }

                // Optimization: if the intersection becomes empty, stop looking
                if (intersectingItems.isEmpty()) break;

            } else {
                NeutronTools.LOGGER.error("Invalid tag: {}", tag);
            }
        }

        if (intersectingItems != null) {
            allItems.addAll(intersectingItems);
        }

        return allItems;
    }

}