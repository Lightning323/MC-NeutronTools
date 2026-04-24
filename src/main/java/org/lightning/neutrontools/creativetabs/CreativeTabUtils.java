package org.lightning.neutrontools.creativetabs;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class CreativeTabUtils {


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
                    applyNBT(stack, nbtString);
                } catch (Exception e) {
                    NeutronTools.LOGGER.error("Failed to Process NBT for Item: {}; NBT: {}",
                            name, nbtString, e);
                }
            }
        }
        return stack;
    }

    public static void applyNBT(ItemStack stack, String nbtString) throws CommandSyntaxException {
        //TODO: Make this more efficient, use the syntax loading in the give command to parse the nbt
        CompoundTag tag = TagParser.parseTag(nbtString);

        // 2. Check if the tag contains 'display.Name' (Old Format)
        // OR if you just passed a tag like {customName: "..."}
        if (tag.contains("customName")) {
            String nameJson = tag.getString("customName");
            // Convert the JSON string to a Component and set the specific component
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(nameJson));

            // Remove it so it doesn't clutter CUSTOM_DATA
            tag.remove("customName");
        }
        if (tag.contains("potion")) {
            String potionType = tag.getString("potion"); // e.g., "minecraft:swiftness"
            // Create the PotionContents record and set the component
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
                    Optional.of(BuiltInRegistries.POTION.getHolderOrThrow(
                            ResourceKey.create(Registries.POTION, ResourceLocation.parse(potionType)))),
                    Optional.empty(), List.of()));
            tag.remove("potion");
        }
        // 3. Apply everything else to CUSTOM_DATA
        if (!tag.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }


    public static ItemStack makeItemStack(String itemId) {
        if (itemId == null) return ItemStack.EMPTY;
        Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId));
        return itemOptional.map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
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
//        NeutronCreativeTabs.getItemsFromUnregisteredTabs().forEach(stack -> {
//            if (stack.getItem() == item) {
//                found.set(true);
//            }
//        });
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
