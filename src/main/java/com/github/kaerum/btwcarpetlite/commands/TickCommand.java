package com.github.kaerum.btwcarpetlite.commands;

import com.github.kaerum.btwcarpetlite.utils.Messenger;
import com.github.kaerum.btwcarpetlite.utils.TickSpeed;
import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.src.EntityPlayer;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.github.kaerum.btwcommander.adaptations.brigadier.BrigadierAdapter.literal;
import static com.github.kaerum.btwcommander.adaptations.brigadier.BrigadierAdapter.argument;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class TickCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return literal("tick")
                .requires((p) -> true)
                .then(literal("rate")
                        .executes((c) -> queryTps(c.getSource()))
                        .then(argument("rate", floatArg(0.1F, 500F))
                                .suggests((context, builder) -> suggestMatching(new String[]{"20.0"}, builder))
                                .executes((context -> setTps(context.getSource(), getFloat(context, "rate"))))))
                .then(literal("warp")
                        .executes((context -> setWarp(context.getSource(), 0, null)))
                        .then(argument("ticks", integer(0, 4_000_000))
                                .suggests(((context, builder) -> suggestMatching(new String[]{"3600", "72000"}, builder)))
                                .executes((context -> setWarp(context.getSource(), getInteger(context, "ticks"), null)))
                                .then(argument("tail command", greedyString())
                                        .executes((context -> setWarp(
                                                context.getSource(),
                                                getInteger(context, "ticks"),
                                                getString(context, "tail command")
                                        ))))))
                .then(literal("health").executes((context -> healthReport(context.getSource()))));
    }

    private static CompletableFuture<Suggestions> suggestMatching(String[] candidates, SuggestionsBuilder builder) {
        String current = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String candidate : candidates) {
            if (shouldSuggest(current, candidate.toLowerCase(Locale.ROOT))) {
                builder.suggest(candidate);
            }
        }
        return builder.buildFuture();
    }

    /**
     * Im not sure what is goin on here, might as well redo this
     * I think it splits candidate by _ and tries to compare those splits and see
     * if they start with current, if so we suggest this candidate.
     */
    private static boolean shouldSuggest(String current, String candidate) {
        for (int i = 0; !candidate.startsWith(current, i); i++) {
            i = candidate.indexOf(95, i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }

    private static int setTps(ServerCommandSource source, float tps) {
        TickSpeed.tickrate(tps);
        queryTps(source);
        return (int)tps;
    }

    private static int queryTps(ServerCommandSource source) {
        Messenger.m(source, "Current TPS is: " + String.format("wb %.1f", TickSpeed.tickrate));
        return (int)TickSpeed.tickrate;
    }

    private static int setWarp(ServerCommandSource source, int advance, String tailCommand) {
        EntityPlayer player = null;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException ignored) {}
        String message = TickSpeed.tickrateAdvance(source.getServer(), player, advance, tailCommand, source);
        if (message != null) {
            source.sendFeedback(message, false);
        }
        return 1;
    }

    private static double average(long[] values) {
        int size = values.length;
        if (size == 0) {
            return 0;
        }
        long sum = 0L;

        for (int i = 0; i < size; i++) {
            sum += values[i];
        }
        return (double)sum / (double)values.length;
    }

    private static int healthReport(ServerCommandSource source) {
        Messenger.m(source, String.format(
                "Avg tick time: %.2f ms",
                average(source.getServer().tickTimeArray) * 1.0E-6D
                )
        );
        return 1;
    }
}
