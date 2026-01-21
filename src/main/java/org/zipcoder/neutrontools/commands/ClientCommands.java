package org.zipcoder.neutrontools.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.data.CreativeTabCustomizationData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zipcoder.neutrontools.NeutronTools;

import java.util.Arrays;

import static org.zipcoder.neutrontools.commands.ModCommands.NAMESPACE;

/**
 * @author HypherionSA
 * Register Client Side Commands
 */
@Mod.EventBusSubscriber(modid = NeutronTools.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(NAMESPACE)
                .then(Commands.literal("creativetabs")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("nameMode")

                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggest(Arrays.stream(CreativeTabCustomizationData.TabNameMode.values())
                                                        .map(Enum::name).map(String::toLowerCase), builder))

                                        .executes(context -> {
                                            String input = StringArgumentType.getString(context, "mode").toUpperCase();
                                            try {
                                                CreativeTabCustomizationData.TabNameMode mode = CreativeTabCustomizationData.TabNameMode.valueOf(input);
                                                CreativeTabCustomizationData.INSTANCE.setTabNameMode(mode);

                                                String msg = switch (mode) {
                                                    case NORMAL -> "Showing standard tab names";
                                                    case TRANSLATION_KEY -> "Showing tab translation keys";
                                                    case RESOURCE_ID -> "Showing tab resource IDs";
                                                };

                                                context.getSource().sendSuccess(() -> Component.literal(msg), true);
                                                return 1;
                                            } catch (IllegalArgumentException e) {
                                                context.getSource().sendFailure(Component.literal("Invalid mode. Use: normal, translation_key, or resource_id"));
                                                return 0;
                                            }
                                        })))
                        .then(Commands.literal("reloadTabs").executes(context -> {
                            CreativeTabs.reloadTabs();
                            context.getSource().sendSuccess(() -> Component.literal("Reloaded Custom Tabs"), true);
                            return 1;
                        }))
                )
        );
    }

}
