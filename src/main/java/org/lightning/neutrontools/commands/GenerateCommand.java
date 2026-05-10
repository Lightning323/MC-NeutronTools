package org.lightning.neutrontools.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.CreativeTabConfig;
import org.lightning.neutrontools.creativetabs.CreativeTabUtils;
import org.lightning.neutrontools.creativetabs.NeutronCreativeTabs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

import static org.lightning.neutrontools.config.CreativeTabsCache.GSON;

public class GenerateCommand {
    private static File basePath = new File("generated");

    private static int generate(String path, CommandContext<CommandSourceStack> context, Predicate<File> function) {

        try {
            File savePath = new File(basePath, path);
            basePath.mkdirs();
            if (function.test(savePath)) {
                Component successMessage = Component.literal("Saved to: ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(savePath.getAbsolutePath()).withStyle(ChatFormatting.GREEN))
                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                context.getSource().sendSuccess(() -> successMessage, true);
                return Command.SINGLE_SUCCESS;
            } else {
                Component errorMessage = Component.literal("Failed to save to: ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(savePath.getAbsolutePath()).withStyle(ChatFormatting.RED))
                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy to clipboard"))));
                context.getSource().sendSuccess(() -> errorMessage, true);
                return Command.SINGLE_SUCCESS;
            }
        }catch (Throwable throwable) {
            NeutronTools.LOGGER.error("Error when generating", throwable);
            Component errorMessage = Component.literal("Error when generating").withStyle(ChatFormatting.RED);
            context.getSource().sendSuccess(() -> errorMessage, true);
            return Command.SINGLE_SUCCESS;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal(NeutronTools.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("generate")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("item_list").executes(context -> {
                            return generate("item_list.txt", context, GenerateCommand::listItemsToFile);
                        }))
                        .then(Commands.literal("block_list").executes(context -> {
                            return generate("block_list.txt", context, GenerateCommand::listBlocksToFile);
                        }))
                        .then(Commands.literal("entity_list").executes(context -> {
                            return generate("entity_list.txt", context, GenerateCommand::listEntitiesToFile);
                        }))
                        .then(Commands.literal("ordered_tabs_list").executes(context ->
                                generate("ordered_tabs.json", context, GenerateCommand::writeOrderedTabs)))
                        .then(Commands.literal("disabled_items_datapack").executes(context ->
                                GenerateCommand.generate("neutron_disabled_items", context, savePath ->
                                        ItemDisabledDatapack.generateDisabledItemsDatapack(
                                                CreativeTabConfig.INSTANCE.disabledItems,
                                                savePath.toPath(),
                                                "neutron_disabled_items"))))
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
        ArrayList<String> blocks = new ArrayList<>();
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
                blocks.add(id.toString());
            }
            Collections.sort(blocks);
            writer.write(String.join("\n", blocks));
            NeutronTools.LOGGER.info("Saved block list to: {}", saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            NeutronTools.LOGGER.error("Failed to save block list ", e);
        }
        return false;
    }


    private static boolean listEntitiesToFile(File saveFile) {
        NeutronTools.LOGGER.info("Saving entity list to {}", saveFile.getAbsolutePath());
        ArrayList<String> entities = new ArrayList<>();
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                entities.add(id.toString());
            }
            Collections.sort(entities);
            writer.write(String.join("\n", entities));
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
            Collections.sort(items);
            Collections.sort(hiddenItems);
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
