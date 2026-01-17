package org.zipcoder.neutrontools.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.zipcoder.neutrontools.NeutronTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = NeutronTools.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    public final static String NAMESPACE = "neutron";

    public static int executeParsedCommandOP(CommandSourceStack originalSource, String command, boolean redirectOutput) {
        MinecraftServer server = originalSource.getServer();
        var dispatcher = server.getCommands().getDispatcher();

        // Remove leading slash
        if (command.startsWith("/")) {
            command = command.substring(1);
        }


        try {
            CommandSourceStack serverSource;

            if (redirectOutput) {
                serverSource = new CommandSourceStack(
                        originalSource.getPlayer(), // entity
                        originalSource.getPlayer().position(), // position
                        originalSource.getPlayer().getRotationVector(), // rotation

                        server.getLevel(originalSource.getPlayer().level().dimension()).getServer()
                                .getLevel(originalSource.getPlayer().level().dimension()), // server level access

                        4, // permission level (OP)
                        originalSource.getPlayer().getName().getString(), // name
                        originalSource.getPlayer().getDisplayName(), // display name
                        server, // server
                        originalSource.getPlayer() // entity again
                ).withPermission(4)
                        .withSuppressedOutput(); // ensure messages show
            } else {
                serverSource = server.createCommandSourceStack()
                        .withPermission(4) // Full OP level
                        .withSuppressedOutput();
            }

            ParseResults<CommandSourceStack> parseResults = dispatcher.parse(command, serverSource);
            return dispatcher.execute(parseResults);
        } catch (CommandSyntaxException e) {
            originalSource.sendFailure(Component.literal("Error executing command: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            originalSource.sendFailure(Component.literal("Error executing command: " + e.getMessage()));
            return 0;
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> root;

    /**
     * Parses and executes a command string using the given source.
     *
     * @param source  The command source.
     * @param command The command to execute (without a leading '/').
     * @return The number of affected entities or a result code.
     */
    public static int executeParsedCommand(CommandSourceStack source, String command) {
        // Use the server's command dispatcher
        MinecraftServer server = source.getServer();
        var dispatcher = server.getCommands().getDispatcher();

        // Parse the command string (if a leading slash exists, remove it)
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        ParseResults<CommandSourceStack> parseResults = dispatcher.parse(command, source);
        try {
            // Execute the parsed command and return the result
            return dispatcher.execute(parseResults);
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("Error executing command: " + e.getMessage()));
            return 0;
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // === Create ONE clean namespace root ===
        root = Commands.literal(NAMESPACE);

        // === /neutron pointcompass <x> <y> <z> (OP only) ===
//        root.then(Commands.literal("pointcompass")
//                .requires(source -> source.hasPermission(2))  // Requires OP level 2
//                .then(Commands.argument("x", IntegerArgumentType.integer())
//                        .then(Commands.argument("y", IntegerArgumentType.integer())
//                                .then(Commands.argument("z", IntegerArgumentType.integer())
//                                        .executes(context -> {
//                                            // Get the command source and coordinates
//                                            CommandSourceStack source = context.getSource();
//                                            int x = IntegerArgumentType.getInteger(context, "x");
//                                            int y = IntegerArgumentType.getInteger(context, "y");
//                                            int z = IntegerArgumentType.getInteger(context, "z");
//
//                                            if (!(source.getEntity() instanceof Player player)) {
//                                                source.sendFailure(Component.literal("This command can only be used by players!"));
//                                                return 0;
//                                            }
//
//                                            // Update the compass
//                                            boolean success = CompassUtils.updateHeldCompass(
//                                                    player,
//                                                    new BlockPos(x, y, z),
//                                                    source.getLevel()
//                                            );
//
//                                            if (!success) {
//                                                source.sendFailure(Component.literal("You must be holding a compass!"));
//                                                return 0;
//                                            }
//
//                                            return 1;
//                                        })
//                                )
//                        )
//                )
//        );

        // === /neutron kill near  (OP only) ===
        root.then(
                Commands.literal("kill")
                        .requires(src -> src.hasPermission(2))
                        .then(
                                Commands.literal("near")
                                        .executes(ctx -> {
                                            executeParsedCommand(ctx.getSource(), "/kill @e[type=!player,distance=..10]");
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );


        // === /neutron locate <player> (OP only) ===
        root.then(
                Commands.literal("locate")
                        .requires(src -> src.hasPermission(2))
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            double x = player.getX();
                                            double y = player.getY();
                                            double z = player.getZ();

                                            MutableComponent literal = Component.literal(
                                                    String.format("%s's position → X: %.2f  Y: %.2f  Z: %.2f",
                                                            player.getName().getString(), x, y, z)
                                            );


//                                            if (ctx.getSource().getEntity() instanceof Player thisPlayer) {
//                                                if (!CompassUtils.updateHeldCompass(
//                                                        thisPlayer, new BlockPos((int) x, (int) y, (int) z), ctx.getSource().getLevel()
//                                                )) {
//                                                    literal.append("\nNote: If a compass is held, it will be updated to point to this player.");
//                                                }
//                                            }

                                            ctx.getSource().sendSuccess(
                                                    () -> literal,
                                                    false
                                            );

                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );

        // === /neutron ping [username]  (EVERYONE allowed!) ===
        root.then(
                Commands.literal("ping")
                        .executes(ctx -> {
                            MinecraftServer server = ctx.getSource().getServer();
                            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                                ping(server, p, ctx.getSource().getPlayer());
                            }
                            return 1;
                        })
                        .then(
                                RequiredArgumentBuilder.<CommandSourceStack, String>argument("username", StringArgumentType.word())
                                        .suggests(usernameSuggestions())
                                        .executes(ctx -> {
                                            String target = StringArgumentType.getString(ctx, "username");
                                            MinecraftServer server = ctx.getSource().getServer();

                                            ServerPlayer found = server.getPlayerList().getPlayerByName(target);
                                            if (found != null) {
                                                ping(server, found, ctx.getSource().getPlayer());
                                                return 1;
                                            }
                                            return 0;
                                        })
                        )
        );

        // === Add crash / overload only if enabled (OP only) ===
        if (NeutronTools.CONFIG.crashCommands) {

            root.then(
                    Commands.literal("crash")
                            .requires(src -> src.hasPermission(2))
                            .executes(ctx -> {

                                new Thread(() -> {
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                    System.exit(1);
                                }).start();

                                ctx.getSource().sendSuccess(() -> Component.literal("Crash command executed"), false);
                                return Command.SINGLE_SUCCESS;
                            })
            );

            root.then(
                    Commands.literal("overload")
                            .requires(src -> src.hasPermission(2))
                            .executes(ctx -> {
                                for (int i = 0; i < 100; i++) {
                                    new Thread(() -> {
                                        List<int[]> list = new ArrayList<>();
                                        while (true) {
                                            list.add(new int[1000000]);
                                        }
                                    }).start();
                                }
                                return Command.SINGLE_SUCCESS;
                            })
            );
        }

        // === Register root ONCE ===
        dispatcher.register(root);

        // Keep your ListAllCommand separate
        ListAllCommand.register(dispatcher);
    }


    private static void ping(MinecraftServer server, ServerPlayer asking, ServerPlayer user) {
        System.out.println("Pinging " + asking.getName().getString());
//        int out = executeParsedCommandOP("spark ping --player " + asking.getName().getString(),true );
        int ping = asking.connection.player.latency;
        ChatFormatting color = ChatFormatting.GREEN;
        if (ping > 200) {
            color = ChatFormatting.RED;
        } else if (ping > 100) {
            color = ChatFormatting.YELLOW;
        }

        ChatFormatting finalColor = color;
        user.sendSystemMessage(
                Component.literal("Ping for \"" + asking.getName().getString() + "\": ")
                        .append(Component.literal(ping + "ms").
                                withStyle(style -> style
                                        .withColor(finalColor) // color it differently
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                asking.getName().getString() + " has " + ping + "ms ping"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy")))
                                )
                        )

        );
    }

    private static SuggestionProvider<CommandSourceStack> usernameSuggestions() {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            List<String> suggestions = new ArrayList<>();
            Collection<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
            players.forEach(player -> suggestions.add(player.getName().getString()));
            return SharedSuggestionProvider.suggest(suggestions, builder);
        };
    }

}