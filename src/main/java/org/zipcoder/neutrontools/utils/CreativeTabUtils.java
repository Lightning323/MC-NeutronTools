package org.zipcoder.neutrontools.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.item.CreativeModeTab;
import org.zipcoder.neutrontools.creativetabs.client.data.NewTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabCustomizationData;
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

    public static String fileToTab(String input) {
        input = input.replace(NeutronTools.RESOURCE_ID + "/", "");
        input = input.replace(NeutronTools.RESOURCE_ID, "");
        input = input.replace(".json", "");

        return input;
    }

    public static Optional<Pair<NewTabJsonHelper, List<ItemStack>>> replacementTab(String tabName) {
        if (CreativeTabCustomizationData.INSTANCE.getReplacedTabs().containsKey(tabName)) {
            return Optional.of(CreativeTabCustomizationData.INSTANCE.getReplacedTabs().get(tabName));
        }
        if (CreativeTabCustomizationData.INSTANCE.getReplacedTabs().containsKey(tabName.toLowerCase())) {
            return Optional.of(CreativeTabCustomizationData.INSTANCE.getReplacedTabs().get(tabName.toLowerCase()));
        }
        return Optional.empty();
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
        ItemStack stack = makeItemStack(item.getName());
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

    public static Set<Item> getItemsByItemMatch(ItemMatch match) {
        Set<Item> allItems = new HashSet<>();

        if (match.tags != null) {
            var tagManager = ForgeRegistries.ITEMS.tags();
            for (String tag : match.tags) {
                if (ResourceLocation.isValidResourceLocation(tag)) {
                    ResourceLocation location = new ResourceLocation(tag);
//                    System.out.println("Tag: " + tag+" location: "+location);
                    // 1. Create the TagKey
                    TagKey<Item> tagKey = tagManager.createTagKey(location);
                    // 2. Access the Tag Manager for this specific key
                    ITag<Item> tagContents = tagManager.getTag(tagKey);

                    if (!tagManager.isKnownTagName(tagKey)) {
                        NeutronTools.LOGGER.error("No known tag name found for: {}", tagKey);
                    }

//                    System.out.println("Tag contents: "+tagContents.size());
//                    System.out.println("Is Tag Bound: " + tagManager.isKnownTagName(tagKey));
//                    System.out.println("Tag Key: " + tagKey.location());
//                    System.out.println("Direct Registry Check: " + ForgeRegistries.ITEMS.tags().getTag(tagKey).stream().count());

                    // 3. Add all items from this tag to our master set
                    if (!tagContents.isEmpty()) {
                        tagContents.stream().forEach(allItems::add);
                    }
                } else {
                    NeutronTools.LOGGER.error("Invalid tag: {}", tag);
                }
            }
        }

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

    public static Set<Item> getItemsByTagList(List<ResourceLocation> tagLocations) {
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


}
