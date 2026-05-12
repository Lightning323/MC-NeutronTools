package org.lightning.neutrontools.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.MerchantOffer;
import org.lightning.neutrontools.NeutronTools;

public class ItemUtils {
    public static String getItemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static boolean isDisabled(String itemid) {
        if (itemid.equals("minecraft:air")) return false;
        return NeutronTools.CONFIG_DISABLED_ITEMS.disabledItemIds.contains(itemid);
    }

    public static boolean isDisabled(Item item) {
        if (item == null || item.equals(Items.AIR)) return false;
        return NeutronTools.CONFIG_DISABLED_ITEMS.disabledItems.contains(item);
    }

    public static boolean isDisabled(ItemStack stack) {
        CustomData tag;
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return false;
        }
        return ItemUtils.isDisabled(ItemUtils.getItemId(stack.getItem()));
    }

    public static boolean isDisabled(MerchantOffer offer) {
        if (ItemUtils.isDisabled(offer.getResult())) {
            return true;
        }
        if (ItemUtils.isDisabled(offer.getBaseCostA())) {
            return true;
        }
        return offer.getCostB() != null && ItemUtils.isDisabled(offer.getCostB());
    }

    public static boolean shouldRecipeBeDisabled(String itemid) {
        return (ItemUtils.isDisabled(itemid));
    }
}