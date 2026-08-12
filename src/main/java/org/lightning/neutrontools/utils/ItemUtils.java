package org.lightning.neutrontools.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.MerchantOffer;
import org.lightning.neutrontools.NeutronTools;

import java.util.List;

public class ItemUtils {

    public static boolean isDisabled(String itemid) {
        if (itemid.equals("minecraft:air")) return false;
        return NeutronTools.CONFIG_DISABLED_ITEMS.matches(itemid);
    }

    public static boolean isDisabled(ItemStack stack) {
        CustomData tag;
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return false;
        }
        return ItemUtils.isDisabled(ResourceUtils.getRegistryID(stack.getItem()));
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