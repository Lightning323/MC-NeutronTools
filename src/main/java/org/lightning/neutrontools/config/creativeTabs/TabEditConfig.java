package org.lightning.neutrontools.config.creativeTabs;

import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Tab edit config is called after the tags and creative tabs have been loaded.
 * We take all that information and make a concrete set of items to add and remove.
 */
public class TabEditConfig {

    public Supplier<ItemStack> tab_icon;
    public String tab_name_key;
    public Map<String, List<ItemStack>> items_to_add;
    public ArrayList<Item> items_to_remove;

    private record StackKey(Item item, DataComponentMap components) {
        static StackKey of(ItemStack stack) {
            return new StackKey(stack.getItem(), stack.getComponents());
        }
    }

    //For the event hook
    public void modifyContentsFromEvent(BuildCreativeModeTabContentsEvent event) {
//        ObjectSortedSet<ItemStack> parentEntries = event.getParentEntries();
//        Set<ItemStack> parentEntries = new HashSet<>(event.getParentEntries());
        Set<StackKey> parentEntries = event.getParentEntries().stream()
                .map(StackKey::of)
                .collect(Collectors.toSet());

        items_to_add.forEach((indx, stacks) -> {
                    stacks.removeIf(is -> {
                        if (is.getCount() != 1) return true; //if itemstack count is not 1, the game will crash

                        StackKey key = StackKey.of(is);
                        // 3. Check if it's already in the master set
                        // .add() returns 'false' if the element was already present!
                        boolean isDuplicate = !parentEntries.add(key);
                        return isDuplicate;
                    });
                    if (indx.isEmpty()) event.acceptAll(stacks);
                    else {
                        Item previous = CreativeTabUtils.getItemByName(indx);
                        if (previous == null ||
                                //If the previous item is not in the parent entries, we should add all items to the end
                                !event.getParentEntries().contains(new ItemStack(previous,1))
                        ) {
                            event.acceptAll(stacks);
                        } else {
                            ItemStack previousStack = new ItemStack(previous, 1);
                            for (ItemStack stack : stacks) {
                                event.insertAfter(previousStack, stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                                previousStack = stack;
                            }
                        }
                    }
                }
        );
        items_to_remove.forEach(item -> event.remove(
                new ItemStack(item, 1),
                CreativeModeTab.TabVisibility.PARENT_TAB_ONLY));
    }


    public TabEditConfig(TabEditJsonRepresentation json) {
        this.tab_name_key = json.tab_name_key;
        if (json.tab_icon != null) this.tab_icon = CreativeTabUtils.makeTabIcon(json.tab_icon.name, json.tab_icon.nbt);
        items_to_add = new HashMap<>();
        items_to_remove = new ArrayList<>();

        if (json.items_to_add != null) {
            json.items_to_add.forEach(itemsEntry -> {
                String index = itemsEntry.after != null ? itemsEntry.after : "";

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