package org.lightning.neutrontools.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
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

        event.getDispatcher().register(
                Commands.literal(NeutronTools.MODID)
                        .then(Commands.literal("creativetabs")
                                .requires(source -> source.hasPermission(2))

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
                                )
                        )
        );
    }
}