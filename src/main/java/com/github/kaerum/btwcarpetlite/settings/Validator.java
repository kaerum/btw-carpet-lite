package com.github.kaerum.btwcarpetlite.settings;

import com.github.kaerum.btwcarpetlite.BTWCarpetLiteServer;
import com.github.kaerum.btwcarpetlite.utils.Messenger;
import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import com.google.common.collect.ImmutableList;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * TODO: ServerCommandSource doesn't exist, adapt.
 * @param <T>
 */

public abstract class Validator<T>
{
    /**
     * Validate the new value of a rule
     * @return true if valid, false if new rule invalid.
     */
    public abstract T validate(ServerCommandSource source, ParsedRule<T> currentRule, T newValue, String string);
    public String description() {return null;}

    public static class _COMMAND<T> extends Validator<T>
    {
        @Override
        public T validate(ServerCommandSource source, ParsedRule<T> currentRule, T newValue, String string)
        {
            if (BTWCarpetLiteServer.settingsManager != null)
                BTWCarpetLiteServer.settingsManager.notifyPlayersCommandsChanged();
            return newValue;
        }
        @Override
        public String description() { return "It has an accompanying command";}
    }

    public static class _COMMAND_LEVEL_VALIDATOR extends Validator<String> {
        private static ImmutableList<String> OPTIONS = ImmutableList.of("true", "false", "ops", "0", "1", "2", "3", "4");
        @Override public String validate(ServerCommandSource source, ParsedRule<String> currentRule, String newValue, String userString) {
            if (!OPTIONS.contains(userString.toLowerCase(Locale.ROOT)))
            {
                Messenger.m(source, "r Valid options for command type rules is 'true' or 'false'");
                Messenger.m(source, "r Optionally you can choose 'ops' to only allow operators");
                Messenger.m(source, "r or provide a custom required permission level");
                return null;
            }
            return userString.toLowerCase(Locale.ROOT);
        }
        public String description() { return "Can be limited to 'ops' only, or a custom permission level";}
    }

    public static class _STRICT<T> extends Validator<T>
    {
        @Override
        public T validate(ServerCommandSource source, ParsedRule<T> currentRule, T newValue, String string)
        {
            if (!currentRule.options.contains(string))
            {
                Messenger.m(source, "r Valid options: " + currentRule.options.toString());
                return null;
            }
            return newValue;
        }
    }

    public static class _STRICT_IGNORECASE<T> extends Validator<T>
    {
        @Override
        public T validate(ServerCommandSource source, ParsedRule<T> currentRule, T newValue, String string)
        {
            if (!currentRule.options.stream().map(s->s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet())
                    .contains(string.toLowerCase(Locale.ROOT)))
            {
                Messenger.m(source, "r Valid options (case insensitive): " + currentRule.options.toString());
                return null;
            }
            return newValue;
        }
    }
}
