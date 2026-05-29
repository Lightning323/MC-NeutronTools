package org.lightning.neutrontools.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import org.lightning.neutrontools.NeutronTools;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.lightning.neutrontools.NeutronTools.*;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.makeItemStack;

public class NeutronDisabledItemConfig {
    public NeutronDisabledItemConfig() {
        loadSimpleJsonLists(new File(BASE_CONFIG_DIRECTORY.toFile(), "disabled_items.json"));
        //-------------------------------------------------------
        //Add disabled items from JEI
        //-------------------------------------------------------
        if (NeutronTools.CONFIG.hideCreativeTabItemsFromJEIBlacklist) {
            Path configDir = FMLPaths.CONFIGDIR.get();
            File jeiBlacklist = new File(configDir.toFile(), "jei/blacklist.cfg");
            if (jeiBlacklist.exists()) {
                try {
                    Files.readAllLines(jeiBlacklist.toPath()).forEach(line -> {
                        if (!line.isBlank()) {
                            String id = line.strip();
                            addDisabledItemID(id);
                        }
                    });
                } catch (IOException e) {
                    NeutronTools.LOG.warn("Failed to process JEI blacklisted items {}", e);
                }
            }
        }
        if (CONFIG.verboseMode) {
            LOG.info("Disabled items: {}", disabledItems);
        }
        loadItems();
    }

    public final Set<Item> disabledItems = new HashSet<>();
    public final Set<String> disabledItemIds = new HashSet<>();

    private void addDisabledItemID(String id) {
        if (id != null && !id.isBlank()) disabledItemIds.add(id);
    }

    public void loadItems() {
        //We need a method to register items themselves because the items are registered at different times for mods
        disabledItems.clear();
        disabledItemIds.forEach(id -> {
            Item i = makeItemStack(id).getItem();
            if (!id.isEmpty()) disabledItems.add(i);
        });
    }

    public boolean isDisabled(ItemStack item) {
        return disabledItems.contains(item);
    }

    public void loadSimpleJsonLists(File file) {
        if (!Files.exists(file.toPath())) {
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
            if (jsonObject.has("disabled_items")) {
                jsonObject.getAsJsonArray("disabled_items").forEach(e -> {
                    addDisabledItemID(e.getAsString());
                });
            }
        }
    }


}
