package org.zipcoder.neutrontools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.zipcoder.neutrontools.mixin.creativeTabs.accessor.CreativeModeTabAccessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.zipcoder.creativetabs.utils.CreativeTabUtils.getTabKey;
import static org.zipcoder.neutrontools.commands.ModCommands.NAMESPACE;

public class ListAllCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        /**
         * List all items
         */
        dispatcher.register(Commands.literal(NAMESPACE)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("listall")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("items").executes(context -> {
                            File savePath = new File("items_list.txt");
                            if (listItemsToFile(savePath))
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("List saved to: " + savePath.getAbsolutePath()), true);
                            else
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("blocks").executes(context -> {
                            File savePath = new File("blocks_list.txt");
                            if (listBlocksToFile(savePath))
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("List saved to: " + savePath.getAbsolutePath()), true);
                            else
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("entities").executes(context -> {
                            File savePath = new File("entities_list.txt");
                            if (listEntitiesToFile(savePath))
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("List saved to: " + savePath.getAbsolutePath()), true);
                            else
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!"), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("creativetabs").executes(context -> {
                            File savePath = new File("creative_mode_tabs.txt");
                            if (listCreativeModeTabsToFile(savePath))
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("List saved to: " + savePath.getAbsolutePath()), true);
                            else
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Failed to save list (path: " + savePath.getAbsolutePath() + ")!"), true);
                            return Command.SINGLE_SUCCESS;
                        })))

        );
    }


    private static boolean listBlocksToFile(File saveFile) {
        System.out.println("Saving block list to: " + saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
//                Block item = BuiltInRegistries.BLOCK.get(id);
                writer.write(id.toString() + "\n");
            }
            System.out.println("Saved block list to: " + saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save block list: " + e.getMessage());
        }
        return false;
    }

    private static boolean listCreativeModeTabsToFile(File saveFile) {
        System.out.println("Saving creative mode tab list to: " + saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.CREATIVE_MODE_TAB.keySet()) {

                CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(id);
                String tabInternalName = getTabKey(((CreativeModeTabAccessor) tab).getInternalDisplayName());

                writer.write(id.toString() + "\t\t" + tabInternalName + "\n");
            }
            System.out.println("Saved creative mode tab list to: " + saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save creative mode tab list: " + e.getMessage());
        }
        return false;
    }

    private static boolean listEntitiesToFile(File saveFile) {
        System.out.println("Saving block list to: " + saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
//                EntityType item = BuiltInRegistries.ENTITY_TYPE.get(id);
                writer.write(id.toString() + "\n");
            }
            System.out.println("Saved block list to: " + saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save block list: " + e.getMessage());
        }
        return false;
    }

    private static boolean listItemsToFile(File saveFile) {
        System.out.println("Saving item list to: " + saveFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(saveFile)) {
            for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
//                Item item = BuiltInRegistries.ITEM.get(id);
                writer.write(id.toString() + "\n");
            }
            System.out.println("Saved item list to: " + saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save item list: " + e.getMessage());
        }
        return false;
    }

}
