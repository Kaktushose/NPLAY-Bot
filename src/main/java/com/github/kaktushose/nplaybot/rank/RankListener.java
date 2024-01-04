package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RankListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RankListener.class);
    private final RankService rankService;
    private final SettingsService settingsService;
    private final EmbedCache embedCache;

    public RankListener(Database database, EmbedCache embedCache) {
        this.rankService = database.getRankService();
        this.settingsService = database.getSettingsService();
        this.embedCache = embedCache;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        log.debug("Received message event");
        var author = event.getAuthor();
        var message = event.getMessage();

        if (author.isBot()) {
            log.trace("Author is bot");
            return;
        }
        if (!event.isFromGuild()) {
            log.trace("Event is not from guild");
            return;
        }

        rankService.increaseTotalMessageCount();

        if (!rankService.isValidMessage(message)) {
            log.trace("Message doesn't meet rank criteria");
            return;
        }

        rankService.updateValidMessage(author);
        var result = rankService.addRandomXp(author);

        log.debug("Checking for rank up: {}", author);
        rankService.updateRankRoles(event.getMember(), event.getGuild(), result);

        if (!result.rankChanged()) {
            log.debug("Rank hasn't changed");
            return;
        }
        log.debug("Applying changes. New rank: {}", result.currentRank());

        var embed = result.nextRank().isPresent() ? "rankIncrease" : "rankIncreaseMax";
        var messageData = new MessageCreateBuilder().addContent(author.getAsMention())
                .addEmbeds(embedCache.getEmbed(embed).injectValues(result.getEmbedValues(author)).toMessageEmbed())
                .build();
        settingsService.getBotChannel(event.getGuild()).sendMessage(messageData).queue();
    }
}
