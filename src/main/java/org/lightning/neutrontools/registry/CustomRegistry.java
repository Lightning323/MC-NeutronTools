package org.lightning.neutrontools.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.block.Block;
import org.lightning.neutrontools.NeutronTools;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Handles registration of custom blocks and items defined via JSON files in the config directory.
 * The JSON format is intentionally simple:
 *
 * <pre>
 * // item JSON example
 * {
 *   "name": "my_custom_item",
 *   "max_stack_size": 64
 * }
 *
 * // block JSON example
 * {
 *   "name": "my_custom_block",
 *   "material": "stone",
 *   "hardness": 1.5,
 *   "resistance": 6.0
 * }
 * </pre>
 *
 * All files must be placed under {@code <config>/neutrontools/registry/items} or {@code .../blocks}.
 * Any missing property falls back to a sensible default.
 */
public class CustomRegistry {
    private static final Logger LOG = NeutronTools.LOG;
    private static final Map<String, Block> BLOCKS = new HashMap<>();

    public static void registerCustomObjects(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            registerBlocks(event);
        } else if (event.getRegistryKey().equals(Registries.ITEM)) {
            registerItems(event);
        }
    }

    private static void registerBlocks(RegisterEvent event) {
        Path blockDir = Paths.get(NeutronTools.BASE_CONFIG_DIRECTORY.toString(), "registry", "blocks");
        if (!Files.isDirectory(blockDir)) {
            return;
        }
        try (Stream<Path> files = Files.list(blockDir)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try {
                    String content = Files.readString(p);
                    JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                    String name = obj.get("name").getAsString();
                    // sanitize registry name (remove namespace, keep simple characters)
                    name = name.replaceAll("[^a-z0-9_/.-]", "");
                    if (name.isEmpty()) {
                        LOG.warn("Custom block JSON {} missing or invalid name", p);
                        return;
                    }
                      // Create default properties. Additional customization can be applied via JSON fields.
                      BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
                      // Override hardness and resistance if provided in the JSON
                      if (obj.has("hardness")) {
                          float hardness = obj.get("hardness").getAsFloat();
                          float resistance = hardness;
                          if (obj.has("resistance")) {
                              resistance = obj.get("resistance").getAsFloat();
                          }
                          properties = properties.strength(hardness, resistance);
                      }

                    Block block = new Block(properties);
                    BLOCKS.put(name, block);
                    event.register(Registries.BLOCK, NeutronTools.resource(name), () -> block);
                    LOG.info("Registered custom block {}", name);
                } catch (Exception e) {
                    LOG.error("Failed to register custom block from {}", p, e);
                }
            });
        } catch (IOException e) {
            LOG.error("Error reading custom block definitions", e);
        }
    }

    private static void registerItems(RegisterEvent event) {
        Path itemDir = Paths.get(NeutronTools.BASE_CONFIG_DIRECTORY.toString(), "registry", "items");
        // Register custom items defined in JSON files
        if (Files.isDirectory(itemDir)) {
            try (Stream<Path> files = Files.list(itemDir)) {
                files.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    try {
                        String content = Files.readString(p);
                        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                        String name = obj.get("name").getAsString();
                        name = name.replaceAll("[^a-z0-9_/.-]", "");
                        if (name.isEmpty()) {
                            LOG.warn("Custom item JSON {} missing or invalid name", p);
                            return;
                        }
                        Properties props = new Properties();
                        if (obj.has("max_stack_size")) {
                            int max = obj.get("max_stack_size").getAsInt();
                            props.stacksTo(max);
                        }
                        // Additional item properties (e.g., fireproof, rarity) can be added here.
                        Item item = new Item(props);
                        event.register(Registries.ITEM, NeutronTools.resource(name), () -> item);
                        LOG.info("Registered custom item {}", name);
                    } catch (Exception e) {
                        LOG.error("Failed to register custom item from {}", p, e);
                    }
                });
            } catch (IOException e) {
                LOG.error("Error reading custom item definitions", e);
            }
        }

        // Register BlockItems for any blocks that do not already have an explicit item JSON
        Path explicitItemDir = itemDir; // reuse for existence check
        for (Map.Entry<String, Block> entry : BLOCKS.entrySet()) {
            String name = entry.getKey();
            Block block = entry.getValue();
            Path possibleItemFile = explicitItemDir.resolve(name + ".json");
            if (Files.exists(possibleItemFile)) {
                // The user provided a separate item definition, skip auto BlockItem.
                continue;
            }
            Item blockItem = new BlockItem(block, new Properties());
            event.register(Registries.ITEM, NeutronTools.resource(name), () -> blockItem);
            LOG.info("Registered BlockItem for custom block {}", name);
        }
    }
}
