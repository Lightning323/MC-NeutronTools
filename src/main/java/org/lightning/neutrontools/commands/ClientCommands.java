package org.lightning.neutrontools.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.lightning.neutrontools.NeutronTools;
import org.lightning.neutrontools.config.creativeTabs.NeutronCreativeTabConfig;

import java.util.Arrays;

@EventBusSubscriber(
        modid = NeutronTools.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {

        LiteralArgumentBuilder<CommandSourceStack> root;
        root = Commands.literal(NeutronTools.MODID);

        root.then(Commands.literal("creativetabs")
                .then(Commands.literal("nameMode")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggest(
                                                Arrays.stream(NeutronCreativeTabConfig.TabNameMode.values())
                                                        .map(Enum::name)
                                                        .map(String::toLowerCase),
                                                builder
                                        ))
                                .executes(context -> {
                                    String input = StringArgumentType.getString(context, "mode").toUpperCase();

                                    try {
                                        NeutronCreativeTabConfig.TabNameMode mode =
                                                NeutronCreativeTabConfig.TabNameMode.valueOf(input);

                                        NeutronTools.CONFIG_CREATIVE_TABS.setTabNameMode(mode);

                                        String msg = switch (mode) {
                                            case NORMAL -> "Showing standard tab names";
                                            case TRANSLATION_KEY -> "Showing tab translation keys";
                                            case RESOURCE_ID -> "Showing tab resource IDs";
                                        };

                                        context.getSource().sendSuccess(() -> Component.literal(msg), false);
                                        return 1;

                                    } catch (IllegalArgumentException e) {
                                        context.getSource().sendFailure(
                                                Component.literal("Invalid mode. Use: normal, translation_key, or resource_id")
                                        );
                                        return 0;
                                    }
                                })
                        )
                ).then(Commands.literal("reload")
                        .executes(context -> {
                            rebuildAllTabs(context.getSource());
                            return 1;
                        })
                )
        );

        event.getDispatcher().register(root);

    }


    public static void rebuildAllTabs(CommandSourceStack source) {
        Minecraft mc = Minecraft.getInstance();

        // Must run on the client thread
        mc.execute(() -> {
            try {
                if (mc.level == null) return;

                // 1. Get current world features & permission flags
                FeatureFlagSet enabledFeatures = mc.level.enabledFeatures();
                boolean hasPermissions = mc.options.operatorItemsTab().get();

                // 2. Re-build the item cache for EVERY registered tab
                CreativeModeTab.ItemDisplayParameters params =
                        new CreativeModeTab.ItemDisplayParameters(enabledFeatures, hasPermissions, mc.level.registryAccess());

                for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                    // Evaluates DisplayItemsGenerator and repopulates tab's internal item lists
                    tab.buildContents(params);
                }

                // 3. Rebuild the search tree indexes so search queries work
                mc.getConnection().updateSearchTrees();
                source.sendSuccess(() -> Component.literal("Successfully rebuilt tabs"), false);
            } catch (Throwable t) {
                source.sendFailure(Component.literal("Failed to rebuild tabs: " + t.getMessage()));
                NeutronTools.LOG.error("Failed to rebuild tabs", t);
            }

//            // 4. If the player currently has the Creative Inventory OPEN, reload the UI
//            if (mc.screen instanceof CreativeModeInventoryScreen creativeScreen) {
//                // Re-initializes the active screen to update slots and tabs visually
//                creativeScreen.init(mc, creativeScreen.width, creativeScreen.height);
//            }
        });
    }
}