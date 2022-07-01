package de.kaktushose.levelbot.commands.moderation.events;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.database.services.EventService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

@CommandController(value = "contest", category = "Moderation")
@Permission("moderator")
public class ContestEventCommand {

    @Inject
    private EventService eventService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Contest Event",
            usage = "{prefix}contest start <channel> <emoji> | {prefix}contest stop",
            desc = "Bilder Contest Event",
            isSuper = true
    )
    public void onContest(CommandEvent event) {
        event.sendSpecificHelpMessage();
    }

    @Command(
            value = "start",
            name = "Contest Event aktivieren",
            usage = "{prefix}contest start <channel> <emoji>",
            desc = "Startet ein Bilder Contest Event",
            category = "Moderation"
    )
    public void onContestEventStart(CommandEvent event, TextChannel channel, String emote) {
        eventService.startContestEvent(event.getGuild().getIdLong(), channel.getIdLong(), emote);
        event.reply(embedCache.getEmbed("contestEventStart"));
    }

    @Command(
            value = "stop",
            name = "Contest Event deaktivieren",
            usage = "{prefix}contest stop",
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
