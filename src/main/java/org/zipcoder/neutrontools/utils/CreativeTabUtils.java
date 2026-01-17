package org.zipcoder.neutrontools.utils;

import org.zipcoder.neutrontools.creativetabs.client.data.CustomCreativeTabJsonHelper;
import org.zipcoder.neutrontools.creativetabs.client.tabs.CreativeTabCustomizationData;
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
        ItemStack stack = getItemStack(tabIcon.getName());

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


    public static ItemStack getItemStack(String itemId) {
        if (itemId == null) return ItemStack.EMPTY;
        Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(itemId));
        return itemOptional.map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
    }

    public static String prefix(String tabName) {
        return String.format("%s.%s", NeutronTools.RESOURCE_ID, tabName);
    }

    public static String getTabKey(Component component) {
        if (component.getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        return component.getString();
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
}
