package com.github.kaktushose.nplaybot.starboard;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.karma.KarmaListener;
import com.github.kaktushose.nplaybot.karma.KarmaService;
import com.github.kaktushose.nplaybot.rank.RankService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StarboardListener extends ListenerAdapter {

    private final StarboardService starboardService;
    private final KarmaService karmaService;
    private final RankService rankService;
    private final KarmaListener karmaListener;

    public StarboardListener(Database database, EmbedCache embedCache) {
        this.starboardService = database.getStarboardService();
        this.karmaService = database.getKarmaService();
        this.rankService = database.getRankService();
        this.karmaListener = new KarmaListener(database, embedCache);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (!event.getReaction().getEmoji().equals(Emoji.fromUnicode("⭐"))) {
            return;
        }

        AtomicInteger count = new AtomicInteger(0);
        event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
            Optional.ofNullable(message.getReaction(Emoji.fromFormatted("⭐"))).ifPresent(
                    it -> count.set(it.getCount())
            );

            long messageId = event.getMessageIdLong();
            if (!starboardService.entryExists(messageId)) {
                starboardService.createEntry(messageId);
            }
            if (count.get() < starboardService.getThreshold(event.getGuild())) {
                return;
            }
            if (!starboardService.isRewarded(messageId)) {
                starboardService.setRewarded(messageId);

                var oldKarma = rankService.getUserInfo(event.getUser()).karma();
                karmaService.addKarma(UserSnowflake.fromId(event.getMessageAuthorIdLong()), starboardService.getKarmaReward(event.getGuild()));
                var newKarma = rankService.getUserInfo(event.getUser()).karma();
                karmaListener.onKarmaIncrease(oldKarma, newKarma, event.getMember(), event.getGuild());
            }

            var starboardChannel = event.getGuild().getTextChannelById(starboardService.getStarboardChannelId(event.getGuild()));
            if (starboardService.isPosted(messageId)) {
                starboardChannel.retrieveMessageById(starboardService.getPostId(messageId))
                        .flatMap(msg -> msg.editMessage(MessageEditData.fromCreateData(buildMessage(message, count.get()))))
                        .queue();
                return;
            }

            starboardChannel.sendMessage(buildMessage(message, count.get())).queue(msg -> starboardService.setPostId(messageId, msg.getIdLong()));
        });
    }

    // single reaction removed
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (!event.getReaction().getEmoji().equals(Emoji.fromUnicode("⭐"))) {
            return;
        }

        AtomicInteger count = new AtomicInteger(0);
        var starboardChannel = event.getGuild().getTextChannelById(starboardService.getStarboardChannelId(event.getGuild()));
        event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
            Optional.ofNullable(message.getReaction(Emoji.fromFormatted("⭐"))).ifPresent(
                    it -> count.set(it.getCount())
            );

            long messageId = event.getMessageIdLong();

            if (count.get() == 0) {
                if (!starboardService.entryExists(messageId)) {
                    return;
                }
                if (!starboardService.isPosted(messageId)) {
                    return;
                }
                starboardChannel.retrieveMessageById(starboardService.getPostId(messageId)).flatMap(Message::delete).queue();
                return;
            }

            if (!starboardService.entryExists(messageId)) {
                starboardService.createEntry(messageId);
            }

            if (count.get() < starboardService.getThreshold(event.getGuild()) && starboardService.isPosted(messageId)) {
                starboardChannel.retrieveMessageById(starboardService.getPostId(messageId)).flatMap(Message::delete).queue();
                starboardService.setPostId(messageId, -1);
                return;
            }

            starboardChannel.sendMessage(buildMessage(message, count.get())).queue(msg -> starboardService.setPostId(messageId, msg.getIdLong()));
        });
    }

    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        if (!event.getReaction().getEmoji().equals(Emoji.fromUnicode("⭐"))) {
            return;
        }
        if (starboardService.entryExists(event.getMessageIdLong())) {
            removeEntry(event);
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (starboardService.entryExists(event.getMessageIdLong())) {
            removeEntry(event);
        }
    }

    @Override
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {
        if (starboardService.entryExists(event.getMessageIdLong())) {
            removeEntry(event);
        }
    }

    private MessageCreateData buildMessage(Message message, int count) {
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(message.getAuthor().getEffectiveName(), null, message.getAuthor().getEffectiveAvatarUrl())
                .setTimestamp(message.getTimeCreated());

        String content = message.getContentRaw();

        if (!message.getAttachments().isEmpty()) {
            embed.setImage(message.getAttachments().get(0).getUrl());
        } else {
            String regex = "(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                embed.setImage(matcher.group(0));
                if (!content.isEmpty()) {
                    content = content.replace(matcher.group(0), "");
                }
            }
        }

        embed.setDescription(content)
                .setColor(16766720)
                .addField("**Source**", String.format("[Jump!](%s)", message.getJumpUrl()), false);

        return new MessageCreateBuilder()
                .setContent(String.format(":star2: **%d** %s", count, message.getChannel().getAsMention()))
                .setEmbeds(embed.build()).build();
    }

    private void removeEntry(GenericMessageEvent event) {
        starboardService.setPostId(event.getMessageIdLong(), -1);
        event.getGuild().getTextChannelById(starboardService.getStarboardChannelId(event.getGuild()))
                .retrieveMessageById(starboardService.getPostId(event.getMessageIdLong()))
                .flatMap(Message::delete)
                .queue();
    }
}
