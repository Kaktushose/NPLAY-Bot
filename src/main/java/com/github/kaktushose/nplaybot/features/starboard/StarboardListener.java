package com.github.kaktushose.nplaybot.features.starboard;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.BotEvent;
import com.github.kaktushose.nplaybot.events.reactions.karma.KarmaBalanceChangeEvent;
import com.github.kaktushose.nplaybot.events.reactions.starboard.StarboardPostDeleteEvent;
import com.github.kaktushose.nplaybot.events.reactions.starboard.StarboardPostUpdateEvent;
import com.github.kaktushose.nplaybot.features.karma.KarmaService;
import com.github.kaktushose.nplaybot.features.rank.RankService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StarboardListener {

    private static final Logger log = LoggerFactory.getLogger(StarboardListener.class);
    private final StarboardService starboardService;
    private final KarmaService karmaService;
    private final RankService rankService;

    public StarboardListener(Bot bot) {
        this.starboardService = bot.getDatabase().getStarboardService();
        this.karmaService = bot.getDatabase().getKarmaService();
        this.rankService = bot.getDatabase().getRankService();
    }

    @BotEvent
    public void onStarboardPostUpdate(StarboardPostUpdateEvent event) {
        var messageId = event.getMessage().getIdLong();
        if (!starboardService.isRewarded(messageId)) {
            starboardService.setRewarded(messageId);

            var oldKarma = rankService.getUserInfo(event.getMember()).karma();
            karmaService.addKarma(event.getMember(), starboardService.getKarmaReward());
            var newKarma = rankService.getUserInfo(event.getMember()).karma();

            event.getEventDispatcher().dispatch(new KarmaBalanceChangeEvent(event.getBot(), oldKarma, newKarma, event.getMember()));
        }

        var starboardChannel = event.getGuild().getTextChannelById(starboardService.getStarboardChannelId());
        if (starboardService.isPosted(messageId)) {
            starboardChannel.retrieveMessageById(starboardService.getPostId(messageId))
                    .flatMap(msg -> msg.editMessage(MessageEditData.fromCreateData(buildMessage(event.getMessage(), event.getVoteCount()))))
                    .queue();
            return;
        }

        starboardChannel.sendMessage(buildMessage(event.getMessage(), event.getVoteCount()))
                .queue(msg -> starboardService.setPostId(messageId, msg.getIdLong()));
    }

    @BotEvent
    public void onStarboardPostDelete(StarboardPostDeleteEvent event) {
        log.info("Deleting starboard post {}", event.getMessageId());
        event.getGuild().getTextChannelById(starboardService.getStarboardChannelId())
                .retrieveMessageById(starboardService.getPostId(event.getMessageId()))
                .flatMap(Message::delete)
                .queue(success -> starboardService.setPostId(event.getMessageId(), -1));
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
}
