package org.lightning.neutrontools.config;

import net.neoforged.fml.loading.FMLPaths;
import org.lightning.neutrontools.NeutronTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lightning.neutrontools.NeutronTools.*;
import static org.lightning.neutrontools.creativetabs.CreativeTabUtils.makeItemStack;

public class DisabledItemConfig extends ItemListConfig {
    public DisabledItemConfig() {
        super("disabled_items.json");
    }

    public void loadFromDisk() {
        itemList.clear();
        itemIdList.clear();
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
                            addItemById(id);
                        }
                    });
                } catch (IOException e) {
                    NeutronTools.LOG.warn("Failed to process JEI blacklisted items {}", e);
                }
            }
        }
        if (CONFIG.verboseMode) {
            LOG.info("Disabled items: {}", this.itemList);
        }

        loadSimpleJsonLists(file);
        loadItems();
    }
}
