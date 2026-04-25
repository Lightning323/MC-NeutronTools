package org.lightning.neutrontools.config.creativeTabs;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Tab edit config is called after the tags and creative tabs have been loaded.
 * We take all that information and make a concrete set of items to add and remove.
 */
public class TabEditConfig {

    public Supplier<ItemStack> tab_icon;
    public String tab_name_key;
    public Map<Integer, List<ItemStack>> items_to_add;
    public ArrayList<Item> items_to_remove;

    public void modifyDisplayItems(Collection<ItemStack> displayItems, Collection<ItemStack> displaySearchItems) {
        //Keep only unique items and Make sure disabled items are removed from list
        Set<CreativeTabUtils.StackFingerprint> seen = new HashSet<>();
        List<ItemStack> uniqueFilteredResult = new ArrayList<>();

        //iterate over displayItems and add unique, non-disabled items only
        for (ItemStack stack : displayItems) {
            Item item = stack.getItem();
            // For 1.12 - 1.20.4: use stack.getTag()
            // For 1.20.5+: use stack.getComponents()
            if ( //TODO: If the item is not added to the JEI blacklist, it might still not be hidden from search
                    seen.add(
                            new CreativeTabUtils.StackFingerprint(stack.getItem(), stack.getComponents()))
                            && !items_to_remove.contains(item) //If the item is not in our tab removal list
                            && !CreativeTabConfig.INSTANCE.disabledItems.contains(item) //If the item is not disabled
            ) {
                uniqueFilteredResult.add(stack);
            }
        }

        //Add all new items to uniqueFilteredResult
        items_to_add.forEach((indx, stacks) -> {
                    if (indx == -1) {
                        uniqueFilteredResult.addAll(stacks);
                    } else {
                        indx = Mth.clamp(indx, 0, stacks.size());
                        uniqueFilteredResult.addAll(indx, stacks);
                    }
                }
        );

        displayItems.clear();
        displaySearchItems.clear();

        displayItems.addAll(uniqueFilteredResult);
        displaySearchItems.addAll(uniqueFilteredResult);
    }

    public TabEditConfig(TabEditJsonRepresentation json) {
        this.tab_name_key = json.tab_name_key;
        if (json.tab_icon != null) this.tab_icon = CreativeTabUtils.makeTabIcon(json.tab_icon.name, json.tab_icon.nbt);
        items_to_add = new HashMap<>();
        items_to_remove = new ArrayList<>();

        if (json.items_to_add != null) {
            json.items_to_add.forEach(itemsEntry -> {
                int index = itemsEntry.index;

                ArrayList<ItemStack> stacks = new ArrayList<>();

                if (itemsEntry.names != null) {
                    for (String name : itemsEntry.names) {
                        ItemStack stack = CreativeTabUtils.makeItemStack(name, itemsEntry.nbt);
                        if (!stack.isEmpty()) stacks.add(stack);
                    }
                }
                if (itemsEntry.match_name != null) {
                    Pattern pattern = Pattern.compile(itemsEntry.match_name);

                    stacks.addAll(BuiltInRegistries.ITEM.entrySet().stream()
                            .filter(entry -> {
                                // Get the ID (e.g., "minecraft:zombie_spawn_egg")
                                String id = entry.getKey().location().toString();
                                return pattern.matcher(id).matches();
                            })
                            .map(entry -> new ItemStack(entry.getValue(), 1))
                            .toList());
                }
                if (itemsEntry.match_tab != null) {
                    stacks.addAll(NeutronTools.TABS.cache.getItemsInCreativeTab(itemsEntry.match_tab, itemsEntry.nbt));
                }
                if (itemsEntry.match_tags != null) {
                    for (String tag : itemsEntry.match_tags) {
                        List<Item> itemsInTag = BuiltInRegistries.ITEM.getOrCreateTag(getTagFromString(tag))
                                .stream()
                                .map(Holder::value)
                                .toList();
                        stacks.addAll(itemsInTag.stream().map(itemInTag -> new ItemStack(itemInTag, 1)).toList());
                    }
                }

                if (items_to_add.containsKey(index)) {
                    items_to_add.get(index).addAll(stacks);
                } else items_to_add.put(index, stacks);
            });
        }

        //Safety clean, to remove invalid items
        items_to_add.values().removeIf(stacks -> stacks.removeIf((is) -> is.getCount() != 1));

        if (json.items_to_remove != null) {
            json.items_to_remove.forEach(item -> {
                ArrayList<Item> stacks = new ArrayList<>();

                if (item.names != null) {
                    for (String name : item.names) {
                        stacks.add(CreativeTabUtils.getItemByName(name));
                    }
                }

                if (item.match_name != null) {
                    Pattern pattern = Pattern.compile(item.match_name);

                    stacks.addAll(BuiltInRegistries.ITEM.entrySet().stream()
                            .filter(entry -> pattern.matcher(entry.getKey().location().toString()).matches())
                            .map(entry -> entry.getValue()) // Returns the Item directly
                            .toList());
                }
                if (item.match_tab != null) {
                    Collection<ItemStack> itemStacks = NeutronTools.TABS.cache.getItemsInCreativeTab(item.match_tab, null);
                    for (ItemStack itemStack : itemStacks) {
                        stacks.add(itemStack.getItem());
                    }
                }
                if (item.match_tags != null) {
                    for (String tag : item.match_tags) {
                        List<Item> itemsInTag = BuiltInRegistries.ITEM.getOrCreateTag(getTagFromString(tag))
                                .stream()
                                .map(Holder::value)
                                .toList();
                        stacks.addAll(itemsInTag);
                    }
                }

                items_to_remove.addAll(stacks);
            });
        }
    }


    public TagKey<Item> getTagFromString(String tagString) {
        // 1. Create a ResourceLocation from the string (e.g., "minecraft:planks")
        ResourceLocation location = ResourceLocation.parse(tagString);
        return TagKey.create(Registries.ITEM, location);
    }


    @Override
    public String toString() {
        return "TabEditConfig{" +
                "\ntab_name_key='" + tab_name_key + '\'' +
                ",\n items_to_add=" + items_to_add +
                ",\n items_to_remove=" + items_to_remove +
                '}';
    }

}