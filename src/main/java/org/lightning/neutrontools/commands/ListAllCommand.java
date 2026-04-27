package org.lightning.neutrontools.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static org.lightning.neutrontools.config.CreativeTabsCache.GSON;

public class ListAllCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal(NeutronTools.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("listall")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("items").executes(context -> {
                            File savePath = new File("items_list.txt");
                            if (listItemsToFile(savePath)) {
                                Component successMessage = Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("blocks").executes(context -> {
                            File savePath = new File("blocks_list.txt");
                            if (listBlocksToFile(savePath)) {
                                Component successMessage = Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("entities").executes(context -> {
                            File savePath = new File("entities_list.txt");
                            if (listEntitiesToFile(savePath)) {
                                Component successMessage = Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("original_tabs").executes(context -> {
                            File savePath = new File("original_tabs.json");
                            if (NeutronCreativeTabs.INSTANCE.cache.writeCacheToFile(savePath)) {
                                Component successMessage = Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("ordered_tabs").executes(context -> {
                            File savePath = new File("ordered_tabs.json");
                            if (writeOrderedTabs(savePath)) {
                                Component successMessage = Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                )

        );
    }


    /**
     * Writes a list of ordered tabs
     *
     * @param saveFile
     * @return
     */
    public static boolean writeOrderedTabs(File saveFile) {
        JsonObject root = new JsonObject();
        JsonArray list = new JsonArray();

        try (FileWriter writer = new FileWriter(saveFile)) {
            NeutronCreativeTabs.INSTANCE.orderedTabs.forEach((tab) ->
                    list.add(CreativeTabUtils.getRegistryID(tab)
                    ));
            root.add("ordered_tabs", list);
            GSON.toJson(root, writer);
            return true;
        } catch (Exception e) {
            NeutronTools.LOGGER.warn("Failed to save list: {}", e.getMessage());
        }
        return false;
    }

    private static boolean listBlocksToFile(File saveFile) {
        NeutronTools.LOGGER.info("Saving block list to {} ", saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
                writer.write(id.toString() + "\n");
            }
            NeutronTools.LOGGER.info("Saved block list to: {}", saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            NeutronTools.LOGGER.error("Failed to save block list ", e);
        }
        return false;
    }


    private static boolean listEntitiesToFile(File saveFile) {
        NeutronTools.LOGGER.info("Saving entity list to {}", saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                writer.write(id.toString() + "\n");
            }
            NeutronTools.LOGGER.info("Saved entity list to: {}", saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            NeutronTools.LOGGER.error("Failed to save entity list: {}", e.getMessage());
        }
        return false;
    }


    private static boolean listItemsToFile(File saveFile) {
        NeutronTools.LOGGER.info("Saving item list to {}", saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            ArrayList<ResourceLocation> items = new ArrayList<>();
            ArrayList<ResourceLocation> hiddenItems = new ArrayList<>();
            for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
                Item item = BuiltInRegistries.ITEM.get(id);

                if (CreativeTabUtils.itemIsVisible(item)) {
                    items.add(id);
                } else {
                    hiddenItems.add(id);
                }
            }
            writer.write("Items:\n");
            for (ResourceLocation id : items) {
                writer.write(id.toString() + "\n");
            }
            writer.write("\n\nHidden Items:\n");
            for (ResourceLocation id : hiddenItems) {
                writer.write(id.toString() + "\n");
            }
            NeutronTools.LOGGER.info("Saved item list to: {}", saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            NeutronTools.LOGGER.error("Failed to save item list: {}", e.getMessage());
        }
        return false;
    }

}
