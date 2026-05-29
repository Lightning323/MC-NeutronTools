package org.lightning.neutrontools.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.lightning.neutrontools.NeutronTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class ItemDisabledDatapack {

    /**
     * recipeIds format:
     * [
     * "minecraft:diamond_sword",
     * "minecraft:iron_pickaxe",
     * "modid:some_recipe"
     * ]
     *
     * @return
     */
    public static boolean generateDisabledItemsDatapack(
            Set<Item> items,
            Path outputFolder,
            String packName
    ) {
        try {
            Path packRoot = outputFolder.resolve(packName);

            // Create pack.mcmeta
            JsonObject mcmeta = new JsonObject();

            JsonObject pack = new JsonObject();
            pack.addProperty("pack_format", 61); // 1.21.1
            pack.addProperty("description", "Auto-generated recipe disabling datapack");

            mcmeta.add("pack", pack);

            Files.createDirectories(packRoot);

            Files.writeString(
                    packRoot.resolve("pack.mcmeta"),
                    mcmeta.toString()
            );

            // Generate disabled recipe files
            for (Item item : items) {
                String recipeId = BuiltInRegistries.ITEM.getKey(item).toString();
                String[] split = recipeId.split(":");

                if (split.length != 2) {
                    NeutronTools.LOG.debug("Invalid recipe id: {}", recipeId);
                    continue;
                }

                String namespace = split[0];
                String recipeName = split[1];

                Path recipePath = packRoot
                        .resolve("data")
                        .resolve(namespace)
                        .resolve("recipe");

                Files.createDirectories(recipePath);

                Path recipeFile = recipePath.resolve(recipeName + ".json");

                JsonObject root = new JsonObject();

                JsonArray conditions = new JsonArray();

                JsonObject falseCondition = new JsonObject();
                falseCondition.addProperty("type", "neoforge:false");

                conditions.add(falseCondition);

                root.add("neoforge:conditions", conditions);

                Files.writeString(recipeFile, root.toString());
            }
            NeutronTools.LOG.debug("Datapack generated at: {}", packRoot.toAbsolutePath());
            return true;
        } catch (IOException e) {
            NeutronTools.LOG.debug("Failed to generate datapack: {}", e.getMessage());
            return false;
        }

    }
}