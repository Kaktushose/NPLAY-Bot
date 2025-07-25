package com.github.kaktushose.nplaybot.rank.daily;

import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;

@Interaction
@Permissions(BotPermissions.USER)
public class SwitchDailyMessageCommand {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;

    @Command(value = "täglich", desc = "Ändert die Einstellungen für die tägliche Kontoinformation")
    public void onCommand(CommandEvent event, @Param(value = "Ob du eine tägliche Nachricht erhalten möchtest oder nicht", name = "aktivieren") boolean enabled) {
        database.getRankService().switchDaily(event.getUser(), enabled);
        event.reply(embedCache.getEmbed("switchDaily").injectValue("switch", enabled ? "aktiviert" : "deaktiviert"));
    }
}
