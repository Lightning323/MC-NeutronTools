package org.lightning.neutrontools.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class ResourceUtils {
    public static String getRegistryID(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }
}
