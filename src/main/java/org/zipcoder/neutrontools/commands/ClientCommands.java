package org.zipcoder.neutrontools.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import org.zipcoder.neutrontools.creativetabs.CreativeTabs;
import org.zipcoder.neutrontools.creativetabs.client.tabs.CreativeTabCustomizationData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zipcoder.neutrontools.NeutronTools;

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
                        .requires(source -> source.hasPermission(2)) // Only players with permission level 2 or higher see this command
                        .then(Commands.literal("showTabNames")
                                .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    CreativeTabCustomizationData.INSTANCE.setShowTabNames(enabled);
                                    context.getSource().sendSuccess(() -> enabled ? Component.literal("Showing tab registry names") : Component.literal("Showing tab names"), true);
                                    return 1;
                                }))).then(Commands.literal("reloadTabs").executes(context -> {
                            CreativeTabs.reloadTabs();
                            context.getSource().sendSuccess(() -> Component.literal("Reloaded Custom Tabs"), true);
                            return 1;
                        }))
                ));
    }

}
