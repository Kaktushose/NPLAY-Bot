package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.services.EventService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

@CommandController("event")
@Permission("moderator")
public class EventCommand {

    @Inject
    private EventService eventService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            value = "balance start",
            name = "Balance Event aktivieren",
            usage = "{prefix}event balance start <id>",
            desc = "Startet das Balance Event mit der angegeben ID",
            category = "Moderation"
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
            value = "balance stop",
            name = "Balance Event deaktivieren",
            usage = "{prefix}event balance stop <id>",
            desc = "Stoppt das Balance Event mit der angegeben ID",
            category = "Moderation"
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
            value = "balance list",
            name = "Balance Event Arten",
            usage = "{prefix}event balance list",
            desc = "Zeigt eine Liste aller verfügbaren Balance Events an",
            category = "Moderation"
    )
    public void onBalanceEventList(CommandEvent event) {
        event.reply(embedCache.getEmbed("balanceEventList"));
    }

    @Command(
            value = "contest start",
            name = "Contest Event aktivieren",
            usage = "{prefix}event contest start <channel> <emoji>",
            desc = "Startet ein Bilder Contest Event",
            category = "Moderation"
    )
    public void onContestEventStart(CommandEvent event, TextChannel textChannel, String emote) {
        eventService.startContestEvent(event.getGuild().getIdLong(), textChannel.getIdLong(), emote);
        event.reply(embedCache.getEmbed("contestEventStart"));
    }

    @Command(
            value = "contest stop",
            name = "Contest Event deaktivieren",
            usage = "{prefix}event contest stop",
            desc = "Stoppt ein Bilder Contest Event",
            category = "Moderation"
    )
    public void onContestEventStop(CommandEvent event) {
        eventService.stopContestEvent(event.getGuild().getIdLong());
        event.reply(embedCache.getEmbed("contestEventStop"));
        List<String> users = eventService.getVoteResult(10, event.getJDA()).getPage();
        EmbedBuilder embedBuilder = embedCache.getEmbed("leaderboard")
                .injectValue("guild", "Contest Event")
                .injectValue("currency", "")
                .toEmbedBuilder();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            result.append(String.format("`%d)` ", i + 1)).append(users.get(i)).append("\n");
        }
        event.reply(embedBuilder.setDescription(result.toString()));
    }
}
