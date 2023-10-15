package com.github.kaerum.btwcarpetlite;

import com.github.kaerum.btwcarpetlite.commands.PlayerCommand;
import com.github.kaerum.btwcarpetlite.commands.TestCommand;
import com.github.kaerum.btwcommander.adaptations.brigadier.BrigadierAdapter;
import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class BTWCarpetLite implements ModInitializer {
    public static final Logger LOGGER = Logger.getLogger("BTWCarpetLite");
    @Override
    public void onInitialize() {
        // Disabled as it is still WIP
//        BrigadierAdapter.register(PlayerCommand.build());
        BrigadierAdapter.register(TestCommand.build());
        BTWCarpetLiteServer.onGameStarted();
    }

}
