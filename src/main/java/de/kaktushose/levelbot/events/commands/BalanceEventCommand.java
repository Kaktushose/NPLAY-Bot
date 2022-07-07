package de.kaktushose.levelbot.events.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.events.data.EventService;

@CommandController(value = "balance", category = "Moderation")
@Permission("moderator")
public class BalanceEventCommand {

    @Inject
    private EventService eventService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Balance Event",
            usage = "{prefix}balance <start|stop> <id> | {prefix}balance list",
            desc = "Balance Events verändern die Ausschüttung der Währungen",
            isSuper = true
    )
    public void onBalance(CommandEvent event) {
        event.sendSpecificHelpMessage();
    }

    @Command(
            value = "start",
            name = "Balance Event aktivieren",
            usage = "{prefix}balance start <id>",
            desc = "Startet das Balance Event mit der angegeben ID"
    )
    public void onBalanceEventStart(CommandEvent event, int id) {
        if (id < 0 || id > 3) {
            event.reply(embedCache.getEmbed("unknownEventId"));
            return;
        }
        String name = eventService.startBalanceEvent(id, event.getGuild().getIdLong());
        event.reply(embedCache.getEmbed("balanceEventStart").injectValue("name", name));
    }

    @Command(
            value = "stop",
            name = "Balance Event deaktivieren",
            usage = "{prefix}balance stop <id>",
            desc = "Stoppt das Balance Event mit der angegeben ID"
    )
    public void onBalanceEventStop(CommandEvent event, int id) {
        if (id < 0 || id > 3) {
            event.reply(embedCache.getEmbed("unknownEventId"));
            return;
        }
        String name = eventService.stopBalanceEvent(id, event.getGuild().getIdLong());
        event.reply(embedCache.getEmbed("balanceEventStop").injectValue("name", name));
    }

    @Command(
            value = "list",
            name = "Balance Event Arten",
            usage = "{prefix}balance list",
            desc = "Zeigt eine Liste aller verfügbaren Balance Events an"
    )
    public void onBalanceEventList(CommandEvent event) {
        event.reply(embedCache.getEmbed("balanceEventList"));
    }

}
