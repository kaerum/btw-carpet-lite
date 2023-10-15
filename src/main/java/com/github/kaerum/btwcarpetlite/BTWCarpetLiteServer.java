package com.github.kaerum.btwcarpetlite;

import com.github.kaerum.btwcarpetlite.settings.SettingsManager;
import net.minecraft.server.MinecraftServer;

public class BTWCarpetLiteServer {
    public static SettingsManager settingsManager;

    public static void onGameStarted() {
        settingsManager = new SettingsManager(
                BTWCarpetLiteSettings.version,
                "btwcarpetlite",
                "BTW Carpet Lite"
        );
        settingsManager.parseSettingsClass(BTWCarpetLiteSettings.class);
    }

    public static void onServerLoad(MinecraftServer server) {
        settingsManager.attachServer(server);
    }

    public static void registerCommands() {
//        AddonHandler.registerCommand();
    }

    public static void onServerClose(MinecraftServer server) {
        settingsManager.detachServer();
    }
}
