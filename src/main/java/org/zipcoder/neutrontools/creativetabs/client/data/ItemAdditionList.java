package org.zipcoder.neutrontools.creativetabs.client.data;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ItemAdditionList {
    private final Map<Integer, List<ItemStack>> itemMap;

    public ItemAdditionList() {
        this.itemMap = new HashMap<>();
    }

    public ItemAdditionList(ItemAdditionList itemAdditionList) {
        //make deep copy
        this.itemMap = new HashMap<>();
        itemAdditionList.itemMap.forEach((key, value) -> {
            this.itemMap.put(key, new ArrayList<>(value));
        });
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
     * Retrieves the list of items for a specific ID.
     * Returns an empty list instead of null to prevent NullPointerExceptions.
     */
    public List<ItemStack> getStacks(int id) {
        return itemMap.getOrDefault(id, Collections.emptyList());
    }

    /**
     * Gets all mapped IDs.
     */
    public Set<Integer> getAllIds() {
        return itemMap.keySet();
    }

    /**
     * Returns a flat list of all ItemStacks, ordered by their map index.
     * If index 1 has [Iron] and index 5 has [Gold], Iron will appear before Gold.
     */
    public List<ItemStack> getAllItemsOrdered() {
        return itemMap.entrySet().stream()
                // Sort by the index (the key)
                .sorted(Map.Entry.comparingByKey())
                // Get the list of items for each index
                .map(Map.Entry::getValue)
                // Flatten the lists into a single stream
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
     * Inserts the stored items into the provided collection at their
     * respective index positions.
     */
    public void apply(Collection<ItemStack> inputStacks) {
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
