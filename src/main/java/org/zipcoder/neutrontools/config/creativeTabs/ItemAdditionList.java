package org.zipcoder.neutrontools.config.creativeTabs;

import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemAdditionList {
    //We make a list of index (position in our tab) and list of items to add
    private final Map<Integer, List<ItemStack>> itemMap;

    public ItemAdditionList() {
        this.itemMap = new HashMap<>();
    }


    /**
     * Adds multiple items to the list for a specific ID.
     * If the ID doesn't exist yet, a new list is initialized.
     */
    public void addStacks(int id, List<ItemStack> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            return;
        }

        // computeIfAbsent ensures we have a valid list to add to
        this.itemMap.computeIfAbsent(id, k -> new ArrayList<>())
                .addAll(newItems);
    }

    /**
     * Adds a single ItemStack to the list associated with the ID.
     * Creates a new list if one doesn't exist.
     */
    public void addStack(int id, ItemStack stack) {
        if (stack == null || stack.getCount() != 1) {
            return;
        }
        itemMap.computeIfAbsent(id, k -> new ArrayList<>()).add(stack);
    }

    /**
     * Gets a unique set of all ItemStacks currently stored.
     */
    public Set<ItemStack> getAllItemStacks() {
        return itemMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Searches through all ID lists and removes any ItemStack that matches the target.
     * @param filter The ItemStack to find and remove.
     */
    public void removeStacksIf(Predicate<ItemStack> filter) {
        itemMap.values().forEach(list -> {
            // Using removeIf with ItemStack.matches ensures we compare
            // the item type and NBT data correctly.
            list.removeIf(filter);
        });
        itemMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Inserts the stored items into the provided collection at their
     * respective index positions.
     */
    public void addItemsInto(Collection<ItemStack> inputStacks) {
        if (inputStacks == null || itemMap.isEmpty()) return;

        // If the collection is a List, we can use positional access
        if (inputStacks instanceof List) {
            List<ItemStack> list = (List<ItemStack>) inputStacks;

            // Iterate through our map of additions
            for (Map.Entry<Integer, List<ItemStack>> entry : itemMap.entrySet()) {
                int index = entry.getKey();
                List<ItemStack> itemsToAdd = entry.getValue();

                //Safety layer to prevent items that dont have proper stack size
                itemsToAdd.removeIf(stack -> stack.getCount() != 1);


                if (list.isEmpty()) {//add relative to beginning
                    list.addAll(itemsToAdd);
                } else if (index >= 0 && index < list.size()) {//add relative to beginning
                    list.addAll(index, itemsToAdd);
                } else if (index == list.size() || index == -1) { //dd to the end
                    list.addAll(itemsToAdd);
                } else if (index < 0) { //add to the end
                    list.addAll(list.size() + index + 1, itemsToAdd);
                }
            }
        } else {
            // If it's just a general collection (like a Set),
            // positional insertion isn't strictly possible, so we just add them.
            itemMap.values().forEach(inputStacks::addAll);
        }
    }

    public int size() {
        return itemMap.size();
    }
}
