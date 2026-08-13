package org.lightning.neutrontools.config;

import java.io.File;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lightning.neutrontools.NeutronTools;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static org.lightning.neutrontools.NeutronTools.*;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.makeItemStack;

public class ItemListConfig {
    final File file;

    public ItemListConfig(String filenameDotJson) {
        if (filenameDotJson.strip().endsWith(".json")) {
            filenameDotJson = filenameDotJson.replace(".json", "");
        }
        file = new File(BASE_CONFIG_DIRECTORY.toFile(), filenameDotJson.strip() + ".json");
        loadFromDisk();
    }

    public final Set<Item> itemList = new HashSet<>();
    public final Set<String> itemIdList = new HashSet<>();


    public Set<Item> getItemList() {
        return itemList;
    }

    public Set<String> getItemIdList() {
        return itemIdList;
    }

    public void addItemById(String id) {
        if (id != null && !id.isBlank()) itemIdList.add(id);
    }

    public void loadItems() {
        //We need a method to register items themselves because the items are registered at different times for mods
        itemList.clear(); //Clear the ACTUAL ITEMS
        itemIdList.forEach(id -> { //Iterate over item ID strings
            Item i = makeItemStack(id).getItem();
            if (!id.isEmpty()) itemList.add(i);
        });
    }

    public boolean matches(ItemStack item) {
        return itemList.contains(item.getItem());
    }

    public boolean matches(Item item) {
        return itemList.contains(item);
    }

    public boolean matches(String itemID) {
        return itemIdList.contains(itemID);
    }

    public void loadFromDisk() {
        itemList.clear();
        itemIdList.clear();
        loadSimpleJsonLists(file);
        loadItems();
    }

    protected void loadSimpleJsonLists(File file) {
        if (!Files.exists(file.toPath())) {
            try {
                Files.writeString(file.toPath(), "{\n\"items\":[]\n}");
            } catch (IOException e) {
                NeutronTools.LOG.warn("Failed to create config file: {}", file, e);
            }
            return;
        }
        JsonObject jsonObject;
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            NeutronTools.LOG.warn("Failed to parse config file: {}", file);
            return;
        }
        if (!jsonObject.isEmpty()) {
            if (jsonObject.has("items")) {
                jsonObject.getAsJsonArray("items").forEach(e -> {
                    addItemById(e.getAsString());
                });
            }
        }
    }


}