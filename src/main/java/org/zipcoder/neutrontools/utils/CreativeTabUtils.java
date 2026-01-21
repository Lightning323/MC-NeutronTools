package org.zipcoder.neutrontools.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.item.CreativeModeTab;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.data.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabEdits;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.zipcoder.neutrontools.NeutronTools;
import net.minecraft.world.item.Items;
import org.zipcoder.neutrontools.creativetabs.client.data.ItemMatch;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;

import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CreativeTabUtils {

    /**
     * Provides the item stack for the tab icon, given the json helper object
     *
     * @param json the json helper
     * @return the item stack for the tab icon
     */
    public static Supplier<ItemStack> makeTabIcon(NewTabJsonHelper json) {
        AtomicReference<ItemStack> icon = new AtomicReference<>(ItemStack.EMPTY);//Our default value
        NewTabJsonHelper.TabIcon tabIcon = new NewTabJsonHelper.TabIcon();

        if (json.getTabIcon() != null) {
            tabIcon = json.getTabIcon();
        }

        /* Resolve the Icon from the Item Registry */
        NewTabJsonHelper.TabIcon finalTabIcon = tabIcon;
        ItemStack stack = makeItemStack(tabIcon.getName());

        if (!stack.isEmpty()) {
            if (finalTabIcon.getNbt() != null && !finalTabIcon.getNbt().isEmpty()) { // Apply the Stack NBT
                //TODO: Understand why this fails with some of the new item groups
                try {
                    CompoundTag tag = TagParser.parseTag(finalTabIcon.getNbt());
                    stack.setTag(tag);
                } catch (Exception e) {
                    NeutronTools.LOGGER.error("Failed to Process NBT for Item Tag: \"{}\";\t Tab Name: \"{}\";\t NBT data: \"{}\"",
                            finalTabIcon.getName(), json.getTabName(), finalTabIcon.getNbt(), e);
                }
            }
            icon.set(stack);
            icon.get().setCount(1);
        }
        if (icon.get().isEmpty()) icon.set(new ItemStack(Items.GRASS_BLOCK, 1));
        return icon::get;
    }


    public static ItemStack makeItemStack(String itemId) {
        if (itemId == null) return ItemStack.EMPTY;
        Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(itemId));
        return itemOptional.map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
    }

    public static String prefix(String tabName) {
        return String.format("%s.%s", NeutronTools.RESOURCE_ID, tabName);
    }

    public static ResourceLocation getRegistryID(CreativeModeTab tab) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
    }

    public static String getTranslationKey(Component component) {
        if (component.getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        return component.getString();
    }

    public static String getTranslationKey(CreativeModeTab tab) {
        return getTranslationKey(((CreativeModeTabAccessor) tab).getInternalDisplayName());
    }

    public static boolean itemIsVisible(Item item) {
        AtomicBoolean found = new AtomicBoolean(false);
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            tab.getDisplayItems().forEach(stack -> {
                if (stack.getItem() == item) {
                    found.set(true);
                }
            });
        }
        CreativeTabs.getItemsFromUnregisteredTabs().forEach(stack -> {
            if (stack.getItem() == item) {
                found.set(true);
            }
        });
        return found.get();
    }

    record StackFingerprint(Item item, Object components) {
    }

    public static List<ItemStack> getUniqueNbtOrderedStacks(Collection<ItemStack> input) {
        // A record to serve as a unique fingerprint for the stack
        // It ignores 'count' but respects Item type and NBT data


        Set<StackFingerprint> seen = new HashSet<>();
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : input) {
            // For 1.12 - 1.20.4: use stack.getTag()
            // For 1.20.5+: use stack.getComponents()
            StackFingerprint fingerprint = new StackFingerprint(stack.getItem(), stack.getTag());

            if (seen.add(fingerprint)) {
                result.add(stack);
            }
        }
        return result;
    }

    public static List<ItemStack> getUniqueOrderedStacks(Collection<ItemStack> input) {
        // This set tracks the singleton Item instances we've already processed
        Set<Item> seenItems = new LinkedHashSet<>();
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : input) {
            // If the item (e.g., Items.IRON_INGOT) is successfully added to the set,
            // it means we haven't seen it yet in this loop.
            if (seenItems.add(stack.getItem())) {
                result.add(stack);
            }
        }
        return result;
    }


    public static Pair<NewTabJsonHelper, List<ItemStack>> getReplacementTab(CreativeModeTab tab) {

        String tabTranslation = getTranslationKey(tab);
        Pair<NewTabJsonHelper, List<ItemStack>> newTabJsonHelperListPair =
                CreativeTabEdits.INSTANCE.getReplacedTabs().get(tabTranslation);
        if (tabTranslation != null && newTabJsonHelperListPair != null) {
//            System.out.println("Found replacement tab! for " + tab);
            return newTabJsonHelperListPair;
        }

        ResourceLocation tabID = getRegistryID(tab);
        if (tabID != null) {
            newTabJsonHelperListPair = CreativeTabEdits.INSTANCE.getReplacedTabs().get(tabID.toString());
            if (newTabJsonHelperListPair != null) {
//                System.out.println("Found replacement tab! for " + tab);
                return newTabJsonHelperListPair;
            }
        }
        return null;
    }


    /**
     * Gets the tab from registry ID or translation key
     *
     * @param key
     * @return
     */
    public static CreativeModeTab getTabFromString(String key) {
        if (ResourceLocation.isValidResourceLocation(key)) {
            try {
                ResourceLocation r = new ResourceLocation(key);
                if (r != null) return BuiltInRegistries.CREATIVE_MODE_TAB.get(r);
            } catch (Exception e) {
                NeutronTools.LOGGER.error("Failed to get tab from registry ID: {}", key, e);
            }
        }

        return BuiltInRegistries.CREATIVE_MODE_TAB.stream()
                .filter(tab -> tab.getDisplayName().getContents() instanceof TranslatableContents translatable
                        && translatable.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public static ItemStack makeStack(NewTabJsonHelper.TabItem item) {
        ItemStack stack = makeItemStack(item.name);
        if (stack.isEmpty()) return stack;

        if (item.nbt != null) {
            if (!item.nbt.isEmpty()) {
                try {
                    CompoundTag tag = TagParser.parseTag(item.nbt);
                    stack.setTag(tag);

                    if (tag.contains("customName"))
                        stack.setHoverName(Component.literal(tag.getString("customName")));
                } catch (CommandSyntaxException e) {
                    NeutronTools.LOGGER.error("Failed to Process NBT for Item {}", item.name, e);
                }
            }
        }
        return stack;
    }

    /**
     * Returns a list of items that match the given item match
     * The match is based on if ALL conditions are met
     * If an item with every tag specified and every name regex specified is found, it will be added to the list
     *
     * @param match
     * @return
     */
    public static Set<Item> getItemsByItemMatch(ItemMatch match) {
        Set<Item> tagMatches = addItemsContainingAllTags(match, new HashSet<>());
        Set<Item> regexMatches = addByNameRegex(match, new HashSet<>());

        // 3. Determine the intersection
        if (match.tags != null && match.tags.length > 0 && match.nameRegex != null) {
            // If BOTH are provided, intersect them
            tagMatches.retainAll(regexMatches);
            return tagMatches;
        } else if (match.nameRegex != null) {
            // Only Regex was provided
            return regexMatches;
        } else {
            // Only Tags were provided (or nothing)
            return tagMatches;
        }
    }

    private static Set<Item> addByNameRegex(ItemMatch match, Set<Item> allItems) {
        if (match.nameRegex != null && !match.nameRegex.isEmpty()) {
            Pattern pattern = Pattern.compile(match.nameRegex);

            allItems.addAll(ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> {
                        // Get the registry name (e.g., "minecraft:iron_ore")
                        String registryName = ForgeRegistries.ITEMS.getKey(item).toString();
                        return pattern.matcher(registryName).matches();
                    })
                    .collect(Collectors.toList()));
        }
        return allItems;
    }

    private static Set<Item> addItemsContainingAllTags(ItemMatch match, Set<Item> allItems) {
        if (match.tags == null || match.tags.length == 0) {
            return allItems;
        }

        var tagManager = ForgeRegistries.ITEMS.tags();
        Set<Item> intersectingItems = null;

        for (String tag : match.tags) {
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

    public static Set<Item> getItemsByTags(List<ResourceLocation> tagLocations) {
        Set<Item> allItems = new HashSet<>();
        var tagManager = ForgeRegistries.ITEMS.tags();

        for (ResourceLocation location : tagLocations) {
            // 1. Create the TagKey
            TagKey<Item> tagKey = tagManager.createTagKey(location);

            // 2. Access the Tag Manager for this specific key
            ITag<Item> tagContents = tagManager.getTag(tagKey);

            // 3. Add all items from this tag to our master set
            if (!tagContents.isEmpty()) {
                tagContents.stream().forEach(allItems::add);
            }
        }

        return allItems;
    }

    public static List<Item> getItemsByRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);

        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> {
                    // Get the registry name (e.g., "minecraft:iron_ore")
                    String registryName = ForgeRegistries.ITEMS.getKey(item).toString();
                    return pattern.matcher(registryName).matches();
                })
                .collect(Collectors.toList());
    }

    public static Item getItemByName(String name) {
        if (ResourceLocation.isValidResourceLocation(name)) {
            ResourceLocation location = new ResourceLocation(name);
            return ForgeRegistries.ITEMS.getValue(location);

        }
        return null;
    }


}
