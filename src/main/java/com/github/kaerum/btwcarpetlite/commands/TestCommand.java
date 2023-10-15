package com.github.kaerum.btwcarpetlite.commands;

import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import static com.github.kaerum.btwcommander.adaptations.brigadier.BrigadierAdapter.literal;


public class TestCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("tes")
                .executes(s -> {
                    s.getSource().sendFeedback("Bitch", false);
                    return 0;
                });
        return literalargumentbuilder;
    }
}
