package com.github.kaktushose.nplaybot.rank.daily;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;

@Interaction
public class SwitchDailyMessageCommand {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;

    @SlashCommand(value = "täglich", desc = "Ändert die Einstellungen für die tägliche Kontoinformation")
    public void onCommand(CommandEvent event, @Param(value = "Ob du eine tägliche Nachricht erhalten möchtest oder nicht", name = "aktivieren") boolean enabled) {
        database.getRankService().switchDaily(event.getUser(), enabled);
        event.reply(embedCache.getEmbed("switchDaily").injectValue("switch", enabled ? "aktiviert" : "deaktiviert"));
    }
}
