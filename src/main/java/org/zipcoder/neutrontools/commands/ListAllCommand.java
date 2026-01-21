package org.zipcoder.neutrontools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.checkerframework.checker.units.qual.A;
import org.zipcoder.neutrontools.NeutronTools;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabCustomizationData;
import org.zipcoder.neutrontools.creativetabs.client.impl.CreativeModeTabMixin_I;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;
import org.zipcoder.neutrontools.utils.CreativeTabUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.zipcoder.neutrontools.utils.CreativeTabUtils.getTranslationKey;
import static org.zipcoder.neutrontools.commands.ModCommands.NAMESPACE;
import static org.zipcoder.neutrontools.utils.CreativeTabUtils.makeItemStack;

public class ListAllCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal(NAMESPACE)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("listall")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("items").executes(context -> {
                            File savePath = new File("items_list.txt");
                            if (listItemsToFile(savePath)) {
                                Component successMessage = net.minecraft.network.chat.Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("blocks").executes(context -> {
                            File savePath = new File("blocks_list.txt");
                            if (listBlocksToFile(savePath)) {
                                Component successMessage = net.minecraft.network.chat.Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("entities").executes(context -> {
                            File savePath = new File("entities_list.txt");
                            if (listEntitiesToFile(savePath)) {
                                Component successMessage = net.minecraft.network.chat.Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("creativetabs").executes(context -> {
                            File savePath = new File("creative_mode_tabs.txt");
                            if (listCreativeModeTabsToFile(savePath)) {
                                Component successMessage = net.minecraft.network.chat.Component.literal("List saved to: ").append(Component.literal(savePath.getAbsolutePath()))
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> successMessage, true);
                            } else {
                                Component errorMessage = net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!")
                                        .withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, savePath.getAbsolutePath()))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal("Copy to clipboard"))));
                                context.getSource().sendSuccess(() -> errorMessage, true);
                            }
                            return Command.SINGLE_SUCCESS;
                        })))

        );
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

    private static boolean listCreativeModeTabsToFile(File saveFile) {
        NeutronTools.LOGGER.info("Saving creative mode tab list to {}", saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            // Header with columns: Tab name (40 chars) | Tab Mod-ID (30 chars)
            writer.write(String.format("%-40s   %-30s%n", "REGISTRY ID", "TRANSLATION KEY"));
            writer.write(String.join("", Collections.nCopies(75, "-")) + "\n");

            // Data rows
            for (ResourceLocation id : BuiltInRegistries.CREATIVE_MODE_TAB.keySet()) {
                CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(id);
                String tabInternalName = getTranslationKey(((CreativeModeTabAccessor) tab).getInternalDisplayName());
                // Left-justified name (40 chars) | Left-justified mod ID (30 chars)
                writer.write(String.format("%-40s   %-30s%n", id.toString(), tabInternalName));
            }
            NeutronTools.LOGGER.info("Saved creative mode tab list to: {}", saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            NeutronTools.LOGGER.error("Failed to save creative mode tab list", e);
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

                AtomicBoolean found = new AtomicBoolean(false);
                for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                    tab.getDisplayItems().forEach(stack -> {
                        if (stack.getItem() == item) {
                            found.set(true);
                        }
                    });
                }

                if (!found.get()) {
                    hiddenItems.add(id);
                } else {
                    items.add(id);
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
