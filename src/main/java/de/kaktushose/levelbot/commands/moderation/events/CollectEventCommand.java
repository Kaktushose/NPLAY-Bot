package de.kaktushose.levelbot.commands.moderation.events;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.database.model.CollectEvent;
import de.kaktushose.levelbot.database.services.EventService;

import java.util.List;

@CommandController(value = "collect", category = "Moderation")
@Permission("moderator")
public class CollectEventCommand {

    @Inject
    private EventService eventService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Collect Event",
            usage = "{prefix}collect <start|stop> <id> | {prefix}collect list",
            desc = "Collect Events mit zusätzlichen Währungen",
            isSuper = true
    )
    public void onCollect(CommandEvent event) {
        event.sendSpecificHelpMessage();
    }

    @Command(
            value = "start",
            name = "Sammel Event aktivieren",
            usage = "{prefix}collect start <id>",
            desc = "Startet das Sammel Event mit der angegeben ID",
            category = "Moderation"
    )
    public void onCollectEventStart(CommandEvent event, int id) {
        if (!eventService.collectEventExistsById(id)) {
            event.reply(embedCache.getEmbed("unknownEventId"));
            return;
        }
        String name = eventService.startCollectEvent(id, event.getGuild());
        event.reply(embedCache.getEmbed("collectEventStart").injectValue("name", name));
    }

    @Command(
            value = "stop",
            name = "Collect Event deaktivieren",
            usage = "{prefix}collect stop",
            desc = "Stoppt das aktuelle Collect Event",
            category = "Moderation"
    )
    public void onCollectEventStop(CommandEvent event) {
        if (eventService.stopCollectEvent(event.getGuild().getIdLong())) {
            event.reply(embedCache.getEmbed("collectEventStop"));
        } else {
            event.reply(embedCache.getEmbed("noActiveCollectEvent"));
        }
    }

    @Command(
            value = "list",
            name = "Collect Event Arten",
            usage = "{prefix}collect list",
            desc = "Zeigt eine Liste aller verfügbaren Collect Events an",
            category = "Moderation"
    )
    public void onCollectEventList(CommandEvent event) {
        List<CollectEvent> events = eventService.getAllCollectEvents();
        StringBuilder builder = new StringBuilder();
        events.forEach(collectEvent -> {
            builder.append(String.format("%d: %s\n", collectEvent.getEventId(), collectEvent.getName()));
        });
        String list = builder.length() == 0 ? "N/A" : builder.substring(0, builder.length() - 1);
        event.reply(embedCache.getEmbed("collectEventList").injectValue("list", list));
    }

}
