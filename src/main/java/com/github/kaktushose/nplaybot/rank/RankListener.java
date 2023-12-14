package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

public class RankListener extends ListenerAdapter {

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
        var author = event.getAuthor();
        var message = event.getMessage();

        if (author.isBot()) {
            return;
        }
        if (!event.isFromGuild()) {
            return;
        }
        if (!rankService.isValidMessage(message)) {
            return;
        }

        rankService.updateValidMessage(author);
        var result = rankService.addRandomXp(author);
        rankService.updateRankRoles(event.getMember(), event.getGuild(), result);

        if (!result.rankChanged()) {
            return;
        }
        var embed = result.nextRank().isPresent() ? "rankIncrease" : "rankIncreaseMax";
        var messageData = new MessageCreateBuilder().addContent(author.getAsMention())
                .addEmbeds(embedCache.getEmbed(embed).injectValues(result.getEmbedValues(author)).toMessageEmbed())
                .build();
        settingsService.getBotChannel(event.getGuild()).sendMessage(messageData).queue();
    }
}
