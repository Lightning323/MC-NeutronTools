package org.lightning.neutrontools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static org.lightning.neutrontools.NeutronTools.CONFIG_PATH;

public class CreativeTabsCache {
    public static final File CREATIVE_TABS_CACHE_FILE = new File(CONFIG_PATH, "cached_original_tabs.json");
    public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //The current state of our original tabs in the form of registry id -> items
    public final HashMap<String, Collection<ItemStack>> originalTabItems = new HashMap<>();

    //The current state of our original tabs
    public List<CreativeModeTab> originalCreativeTabs = new ArrayList<>();

    //The cached state of our original tabs (loaded from the cache file)
    public final HashMap<String, Collection<ItemStack>> cacheFile_originalTabItems = new HashMap<>();

    public CreativeTabsCache() {
        if (hasCacheFile()) {
            readCacheFromFile(CREATIVE_TABS_CACHE_FILE);
        }
    }


    public void buildContents(CreativeModeTab self, Collection<ItemStack> displayItems, Set<ItemStack> displayItemsSearchTab) {
        originalTabItems.put(CreativeTabUtils.getRegistryID(self), new ArrayList<>(displayItems));
        originalCreativeTabs.add(self);
    }

    public Collection<ItemStack> getItemsInCreativeTab(CreativeModeTab tab) {
        /**
         * First, try to get the items if they are cached
         */
        //Get by registry id first, then by translation key if not found
        Collection<ItemStack> itemStacks = originalTabItems.get(CreativeTabUtils.getRegistryID(tab));
        if (itemStacks != null) return itemStacks;

        if (hasCacheFile()) {
            itemStacks = cacheFile_originalTabItems.get(CreativeTabUtils.getRegistryID(tab));
            if (itemStacks != null) return itemStacks;
        }
        return new ArrayList<>();
    }

    private boolean savedCache = false;

    public void writeCache() {
        if (!savedCache) {//We only have to write the cache once
            if (writeCacheToFile(CREATIVE_TABS_CACHE_FILE)) savedCache = true;
        }
    }

    public boolean writeCacheToFile(File saveFile) {
        NeutronTools.LOGGER.info("Saving original creative tab list to {}", saveFile.getAbsolutePath());
        JsonObject root = new JsonObject();
        JsonArray tabsArray = new JsonArray();

        try (FileWriter writer = new FileWriter(saveFile)) {
            // 1. Process Registered Tabs
            originalTabItems
                    .forEach((tabName, items) -> {
                        JsonObject tabJson = new JsonObject();
                        tabJson.addProperty("tab", tabName);

                        JsonArray itemsArray = new JsonArray();
                        for (ItemStack item : items) {
                            itemsArray.add(BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                        }
                        tabJson.add("names", itemsArray);
                        tabsArray.add(tabJson);
                    });
            root.add("tabs", tabsArray);
            GSON.toJson(root, writer);

            NeutronTools.LOGGER.info("Saved item list to: {}", saveFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            NeutronTools.LOGGER.warn("Failed to save item list: {}", e.getMessage());
        }
        return false;
    }

    private boolean readCacheFromFile(File loadFile) {
        if (!loadFile.exists()) {
            NeutronTools.LOGGER.warn("No creative tab cache found at {}", loadFile.getAbsolutePath());
            return false;
        }

        NeutronTools.LOGGER.info("Loading original creative tab list from {}", loadFile.getAbsolutePath());
        Gson gson = new Gson();

        try (java.io.FileReader reader = new java.io.FileReader(loadFile)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("tabs")) return false;

            JsonArray tabsArray = root.getAsJsonArray("tabs");

            // Clear old cached data before repopulating
            cacheFile_originalTabItems.clear();

            tabsArray.forEach(element -> {
                JsonObject tabJson = element.getAsJsonObject();
                String tabName = tabJson.get("tab").getAsString();
                JsonArray itemsArray = tabJson.getAsJsonArray("names");

                List<ItemStack> itemStacks = new ArrayList<>();
                itemsArray.forEach(itemElement -> {
                    String itemRegistryName = itemElement.getAsString();

                    // Convert Registry String back to Item and then to ItemStack
                    net.minecraft.resources.ResourceLocation loc = net.minecraft.resources.ResourceLocation.tryParse(itemRegistryName);
                    if (loc != null) {
                        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(loc);
                        // Check if item isn't the 'air' default (meaning registry lookup failed)
                        if (item != net.minecraft.world.item.Items.AIR || itemRegistryName.equals("minecraft:air")) {
                            itemStacks.add(new ItemStack(item));
                        }
                    }
                });

                cacheFile_originalTabItems.put(tabName, itemStacks);
            });

            NeutronTools.LOGGER.info("Successfully loaded {} tabs from cache file.", cacheFile_originalTabItems.size());
            return true;
        } catch (Exception e) {
            NeutronTools.LOGGER.error("Failed to read creative tab cache file: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasCacheFile() {
        return CREATIVE_TABS_CACHE_FILE.exists();
    }

}
