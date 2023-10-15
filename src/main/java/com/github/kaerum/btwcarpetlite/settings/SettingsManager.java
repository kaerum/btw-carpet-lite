package com.github.kaerum.btwcarpetlite.settings;

import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.util.TriConsumer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsManager {

    private Map<String, ParsedRule<?>> rules = new HashMap<>();
    public boolean locked;
    private final String version;
    private final String identifier;
    private final String fancyName;
    private MinecraftServer server;
    private List<TriConsumer<?, ParsedRule<?>, String>> observers = new ArrayList<>();

    public SettingsManager(String version, String identifier, String fancyName) {
        this.version = version;
        this.identifier = identifier;
        this.fancyName = fancyName;
    }

    public void attachServer(MinecraftServer server) {
        this.server = server;
//        loadConfigurationFromConf();
        // adapt
//        registerCommand(server, server.getCommandManager());
//        notifyPlayersCommandsChanged();
    }

    public void detachServer() {
        for (ParsedRule<?> rule : rules.values()) {
            // adapt
//            rule.resetToDefault();
        }
        server = null;
    }

    public void parseSettingsClass(Class settingsClass)
    {
        for (Field f : settingsClass.getDeclaredFields())
        {
            Rule rule = f.getAnnotation(Rule.class);
            if (rule == null) continue;
            ParsedRule parsed = new ParsedRule(f, rule);
            rules.put(parsed.name, parsed);
        }
    }

    public void notifyPlayersCommandsChanged() {
        // Ignored
    }

    public void notifyRuleChanged(ServerCommandSource source, ParsedRule<?> rule, String userTypedValue) {
        // Ignored
    }

    public static boolean canUseCommand(ServerCommandSource source, String commandLevel) {
        switch (commandLevel) {
            case "true": return true;
            case "false": return false;
            case "ops": return source.hasPermissionLevel(2);
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
                return source.hasPermissionLevel(Integer.parseInt(commandLevel));
        }
        return false;
    }

}
