package org.zipcoder.neutrontools.utils;

import net.minecraft.world.item.CreativeModeTab;
import org.zipcoder.neutrontools.creativetabs.client.data.CustomCreativeTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.CreativeTabCustomizationData;
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
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CreativeTabUtils {

    /**
     * Provides the item stack for the tab icon, given the json helper object
     *
     * @param json the json helper
     * @return the item stack for the tab icon
     */
    public static Supplier<ItemStack> makeTabIcon(CustomCreativeTabJsonHelper json) {
        AtomicReference<ItemStack> icon = new AtomicReference<>(ItemStack.EMPTY);//Our default value
        CustomCreativeTabJsonHelper.TabIcon tabIcon = new CustomCreativeTabJsonHelper.TabIcon();

        if (json.getTabIcon() != null) {
            tabIcon = json.getTabIcon();
        }

        /* Resolve the Icon from the Item Registry */
        CustomCreativeTabJsonHelper.TabIcon finalTabIcon = tabIcon;
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

    public static Optional<Pair<CustomCreativeTabJsonHelper, List<ItemStack>>> replacementTab(String tabName) {
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
        if (isValidRegistryId(key)) {
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

    /**
     * Determines if a string is a valid format for a Registry ID (ResourceLocation).
     * Valid format is "namespace:path" (e.g., "minecraft:iron_ingot")
     * using only lowercase a-z, 0-9, dot, underscore, and dash.
     */
    public static boolean isValidRegistryId(String key) {
        return ResourceLocation.isValidResourceLocation(key);
    }


}
