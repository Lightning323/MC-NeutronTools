package org.zipcoder.neutrontools.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.zipcoder.neutrontools.creativetabs.NeutronCreativeTabs;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CreativeTabUtils {

    public static CreativeModeTab makeNewTab(String titleKey, Supplier<ItemStack> icon) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        builder.title(Component.translatable(prefix(titleKey)));
        builder.icon(icon);
        return builder.build();
    }

    public static Supplier<ItemStack> makeTabIcon(String name, String nbtString) {
        ItemStack stack = makeItemStack(name, nbtString);
        if (stack.isEmpty()) return () -> new ItemStack(Items.GRASS_BLOCK, 1);
        return () -> stack;
    }

    public static ItemStack makeItemStack(String name, String nbtString) {
        ItemStack stack = makeItemStack(name);

        if (!stack.isEmpty()) {
            if (nbtString != null && !nbtString.isEmpty()) {
                try {
                    // 1. Parse the string into a CompoundTag (this still works)
                    CompoundTag tag = TagParser.parseTag(nbtString);

                    // 2. Use .set() with the CUSTOM_DATA component
                    // CustomData.of(tag) wraps the NBT for the new component system
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(tag));

                } catch (Exception e) {
                    NeutronTools.LOGGER.error("Failed to Process NBT for Item: {}; NBT: {}",
                            name, nbtString, e);
                }
            }
        }
        return stack;
    }


    public static ItemStack makeItemStack(String itemId) {
        if (itemId == null) return ItemStack.EMPTY;
        Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId));
        return itemOptional.map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
    }

    public static String prefix(String tabName) {
        return String.format("%s.%s", NeutronTools.RESOURCE_ID, tabName);
    }

    public static String getTranslationKey(CreativeModeTab tab) {
        return getTranslationKey(((CreativeModeTabAccessor) tab).getInternalDisplayName());
    }

    public static String getTranslationKey(Component component) {
        if (component.getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        return component.getString();
    }

    public static String getRegistryID(CreativeModeTab tab) {
        ResourceLocation res = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
        if (res != null) return res.toString();
        else return "";
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
        NeutronCreativeTabs.getItemsFromUnregisteredTabs().forEach(stack -> {
            if (stack.getItem() == item) {
                found.set(true);
            }
        });
        return found.get();
    }

    public record StackFingerprint(Item item, Object components) {
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


    /**
     * Gets the tab from registry ID or translation key
     */
    public static CreativeModeTab getTabFromString(String key) {
        // 1. Use tryParse to handle the ID lookup safely
        ResourceLocation r = ResourceLocation.tryParse(key);
        if (r != null) {
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(r);
            // BuiltInRegistries.get() returns null if not found in 1.21.1
            if (tab != null) return tab;
        }

        // 2. Fallback to checking translation keys for Vanilla/Registered tabs
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            if (getTranslationKey(tab).equals(key)) {
                return tab;
            }
        }

        // 3. Check your custom injected tabs
        for (CreativeModeTab tab : CreativeTabConfig.INSTANCE.newTabs) {
            if (getTranslationKey(tab).equals(key)) {
                return tab;
            }
        }

        return null;
    }


    public static Item getItemByName(String name) {
        // 1. ResourceLocation constructor is now private.
        // Use .parse() or .tryParse()
        ResourceLocation location = ResourceLocation.tryParse(name);

        if (location != null) {
            // 2. Use BuiltInRegistries.ITEM to get the value.
            // It returns Items.AIR (which is the modern "null") if not found.
            Item item = BuiltInRegistries.ITEM.get(location);

            // Optional: If you strictly want null instead of Air for your logic
            return item == Items.AIR ? null : item;
        }

        return null;
    }


}
