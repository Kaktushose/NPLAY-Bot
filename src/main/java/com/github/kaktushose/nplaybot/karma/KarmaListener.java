package com.github.kaktushose.nplaybot.karma;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.rank.RankService;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import static com.github.kaktushose.nplaybot.items.ItemExpirationTask.PLAY_ACTIVITY_KARMA_THRESHOLD;

public class KarmaListener extends ListenerAdapter {

    private final KarmaService karmaService;
    private final RankService rankService;
    private final EmbedCache embedCache;

    public KarmaListener(Database database, EmbedCache embedCache) {
        karmaService = database.getKarmaService();
        rankService = database.getRankService();
        this.embedCache = embedCache;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!rankService.isValidChannel(event.getChannel(), event.getGuild())) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        if (event.getUser().getIdLong() == event.getMessageAuthorIdLong()) {
            return;
        }
        if (!karmaService.getValidEmojis(event.getGuild()).contains(event.getEmoji())) {
            return;
        }
        int oldKarma = rankService.getUserInfo(UserSnowflake.fromId(event.getMessageAuthorIdLong())).karma();
        int newKarma = karmaService.onKarmaVoteAdd(event.getUser(), UserSnowflake.fromId(event.getMessageAuthorIdLong()));
        event.retrieveMessage().queue(message -> karmaService.onKarmaIncrease(oldKarma, newKarma, message.getMember(), message.getGuild(), embedCache));
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!rankService.isValidChannel(event.getChannel(), event.getGuild())) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        if (!karmaService.getValidEmojis(event.getGuild()).contains(event.getEmoji())) {
            return;
        }
        event.retrieveMessage().queue(message -> {
            if (event.getUser().getIdLong() == message.getAuthor().getIdLong()) {
                return;
            }
            int oldKarma = rankService.getUserInfo(message.getAuthor()).karma();
            int newKarma = karmaService.onKarmaVoteRemove(event.getUser(), message.getAuthor());
            karmaService.onKarmaDecrease(oldKarma, newKarma, message.getMember(), message.getGuild(), embedCache);
        });
    }
}
