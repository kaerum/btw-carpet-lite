package com.github.kaerum.btwcarpetlite.commands;

import com.github.kaerum.btwcarpetlite.BTWCarpetLite;
import com.github.kaerum.btwcarpetlite.BTWCarpetLiteSettings;
import com.github.kaerum.btwcarpetlite.patches.EntityPlayerMPFake;
import com.github.kaerum.btwcarpetlite.settings.SettingsManager;
import com.github.kaerum.btwcarpetlite.utils.Messenger;
import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import com.github.kaerum.btwcommander.adaptations.brigadier.argumenttypes.RotationArgumentType;
import com.github.kaerum.btwcommander.adaptations.brigadier.argumenttypes.Vec3ArgumentType;
import com.github.kaerum.btwcommander.math.Vec2f;
import com.google.common.collect.Sets;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static com.github.kaerum.btwcommander.adaptations.brigadier.BrigadierAdapter.argument;
import static com.github.kaerum.btwcommander.adaptations.brigadier.BrigadierAdapter.literal;

public class PlayerCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("player")
                .requires((player) -> SettingsManager.canUseCommand(player, BTWCarpetLiteSettings.commandPlayer))
                .then(argument("player", StringArgumentType.word())
                        .then(literal("spawn").executes(PlayerCommand::spawn)
                                .then(literal("at").then(argument("position", Vec3ArgumentType.vec3()).executes(PlayerCommand::spawn)
                                        .then(literal("facing").then(argument("direction", RotationArgumentType.rotation()).executes(PlayerCommand::spawn)
                                                .then(literal("in").then(argument("dimension", IntegerArgumentType.integer(-1, 1)).executes(PlayerCommand::spawn)))
                                        ))
                                ))
                        )
                        .then(literal("kill").executes(PlayerCommand::kill))
                );
        return literalargumentbuilder;
    }

    private static Collection<String> getPlayers(ServerCommandSource source)
    {
        Set<String> players = Sets.newLinkedHashSet(Arrays.asList("Steve", "Alex"));
        return players;
    }

    private static EntityPlayerMP getPlayer(CommandContext<ServerCommandSource> context)
    {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getServer();
        return server.getConfigurationManager().getPlayerForUsername(playerName);
    }

    private static boolean cantManipulate(CommandContext<ServerCommandSource> context)
    {
        EntityPlayer player = getPlayer(context);
        if (player == null)
        {
            Messenger.m(context.getSource(), "Can only manipulate existing players");
            return true;
        }
        EntityPlayer sendingPlayer;
        try
        {
            sendingPlayer = context.getSource().getPlayer();
        }
        catch (CommandSyntaxException e)
        {
            return false;
        }

        if (!context.getSource().getServer().getConfigurationManager().getOps().contains(sendingPlayer.username))
        {
            if (sendingPlayer != player && !(player instanceof EntityPlayerMPFake))
            {
                Messenger.m(context.getSource(), "Non OP players can't control other real players");
                return true;
            }
        }
        return false;
    }

    private static boolean cantReMove(CommandContext<ServerCommandSource> context)
    {
        if (cantManipulate(context)) return true;
        EntityPlayer player = getPlayer(context);
        if (player instanceof EntityPlayerMPFake) return false;
        Messenger.m(context.getSource(), "Only fake players can be moved or killed");
        return true;
    }

    private static boolean cantSpawn(CommandContext<ServerCommandSource> context)
    {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getServer();
        ServerConfigurationManager manager = server.getConfigurationManager();
        EntityPlayer player = manager.getPlayerForUsername(playerName);
        if (player != null)
        {
            Messenger.m(context.getSource(), "Player ", playerName, " is already logged on");
            return true;
        }
        if (manager.getBannedPlayers().isBanned(playerName))
        {
            Messenger.m(context.getSource(), "Player ", playerName, " is banned");
            return true;
        }
        if (manager.isWhiteListEnabled() && manager.getWhiteListedPlayers().contains(playerName) && !context.getSource().hasPermissionLevel(2))
        {
            Messenger.m(context.getSource(), "Whitelisted players can only be spawned by operators");
            return true;
        }
        return false;
    }

    private static int kill(CommandContext<ServerCommandSource> context)
    {
        if (cantReMove(context)) { return 0; }
        getPlayer(context).attackEntityFrom(DamageSource.outOfWorld, 1000);
        return 1;
    }

    @FunctionalInterface
    interface SupplierWithCommandSyntaxException<T>
    {
        T get() throws CommandSyntaxException;
    }

    private static <T> T tryGetArg(SupplierWithCommandSyntaxException<T> a, SupplierWithCommandSyntaxException<T> b) throws CommandSyntaxException
    {
        try
        {
            return a.get();
        }
        catch (IllegalArgumentException e)
        {
            return b.get();
        }
    }

    private static int spawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        if (cantSpawn(context)) return 0;
        ServerCommandSource source = context.getSource();
        Vec3 pos = tryGetArg(
                () -> Vec3ArgumentType.getVec3(context, "position"),
                source::getPosition
        );
        Vec2f facing = tryGetArg(
                () -> RotationArgumentType.getRotation(context, "direction"),
                source::getRotation
        );
        int dim = tryGetArg(
                () -> IntegerArgumentType.getInteger(context, "dimension"),
                () -> source.getPlayer().dimension
        );
        EnumGameType mode = EnumGameType.CREATIVE;
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = source.getServer();
        EntityPlayer player = null;
        try {
            player = EntityPlayerMPFake.createFake(playerName, server, pos.xCoord, pos.yCoord, pos.zCoord, facing.y, facing.x, dim, mode);
        } catch (Exception exception) {
            BTWCarpetLite.LOGGER.info("Failed to create fake player");
            exception.printStackTrace();
        }
        if (player == null)
        {
            Messenger.m(context.getSource(), "Player " + StringArgumentType.getString(context, "player") + " doesn't exist " +
                    "and cannot spawn in online mode. Turn the server offline to spawn non-existing players");
        } else {
            Messenger.m(context.getSource(), "Player created");
        }
        return 1;
    }
}
