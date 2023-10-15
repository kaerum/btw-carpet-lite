package com.github.kaerum.btwcarpetlite;

import com.github.kaerum.btwcarpetlite.settings.Rule;
import static com.github.kaerum.btwcarpetlite.settings.RuleCategory.COMMAND;

@SuppressWarnings("CanBeFinal")
public class BTWCarpetLiteSettings
{
    public static final String version = "0.1";

    @Rule(desc = "Enables /player command to spawn players", category = COMMAND)
    public static String commandPlayer = "true";
}
