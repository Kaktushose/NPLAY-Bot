package com.github.kaktushose.nplaybot.events.contest;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

@Interaction
@Permissions(BotPermissions.MANAGE_EVENTS)
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class ContestCommands {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(value = "events contest-event start", desc = "Startet ein Contest-Event")
    public void onContestEventStart(CommandEvent event,
                                    @Param("Der Textkanal, in dem das Contest-Event stattfinden soll") TextChannel channel,
                                    @Param("Der Emoji, mit dem abgestimmt werden soll") String emoji) {
        database.getContestEventService().startContestEvent(channel, emoji);
        event.reply(embedCache.getEmbed("contestEventStart").injectValue("channel", channel.getAsMention()));
    }

    @Command(value = "events contest-event stop", desc = "Stoppt das aktuelle Contest-Event und zeigt die Gewinner an")
    public void onContestEventStop(CommandEvent event) {
        var result = database.getContestEventService().stopContestEvent();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.size(); i++) {
            var row = result.get(i);
            builder.append(String.format("%d. %s (%d Votes)\n", i, resolveName(event.getGuild(), row.userId()), row.votes()));
        }
        event.reply(embedCache.getEmbed("contestEventStop").injectValue("leaderboard", builder.toString()));
    }


    private String resolveName(Guild guild, long userId) {
        var member = Optional.ofNullable(guild.getMemberById(userId));
        if (member.isPresent()) {
            return member.get().getEffectiveName();
        }
        // retrieve member thus it gets loaded to cache
        guild.retrieveMemberById(userId).queue();
        return String.format("<@%d>", userId);
    }
}
