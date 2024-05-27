package com.github.kaktushose.nplaybot.karma;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
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

public class KarmaListener extends ListenerAdapter {

    private final KarmaService karmaService;
    private final RankService rankService;
    private final SettingsService settingsService;
    private final EmbedCache embedCache;

    public KarmaListener(Database database, EmbedCache embedCache) {
        karmaService = database.getKarmaService();
        rankService = database.getRankService();
        settingsService = database.getSettingsService();
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
        event.retrieveMessage().queue(message -> onKarmaIncrease(oldKarma, newKarma, message.getMember(), message.getGuild()));
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
            onKarmaDecrease(oldKarma, newKarma, message.getMember(), message.getGuild());
        });
    }

    public void onKarmaIncrease(int oldKarma, int newKarma, Member member, Guild guild) {
        var rewards = karmaService.getKarmaRewards();
        var optional = rewards.stream()
                .filter(it -> it.threshold() > oldKarma)
                .filter(it -> it.threshold() <= newKarma)
                .findFirst();

        if (optional.isEmpty()) {
            return;
        }
        var reward = optional.get();

        if (reward.xp() > 0) {
            var xpChangeResult = rankService.addXp(member, reward.xp());
            rankService.onXpChange(xpChangeResult, member, guild, embedCache).ifPresent(it ->
                    settingsService.getBotChannel(guild).sendMessage(it).queue()
            );
        }

        if (reward.roleId() > 0) {
            guild.addRoleToMember(member, guild.getRoleById(reward.roleId())).queue();
        }

        var builder = new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(EmbedBuilder.fromData(DataObject.fromJson(reward.embed())).build())
                .build();
        settingsService.getBotChannel(guild).sendMessage(builder).queue();
    }

    private void onKarmaDecrease(int oldKarma, int newKarma, Member member, Guild guild) {
        var rewards = karmaService.getKarmaRewards();
        var optional = rewards.stream()
                .filter(it -> it.threshold() < oldKarma)
                .filter(it -> it.threshold() >= newKarma)
                .findFirst();

        if (optional.isEmpty()) {
            return;
        }
        var reward = optional.get();

        if (reward.xp() > 0) {
            var xpChangeResult = rankService.addXp(member, -reward.xp());
            rankService.onXpChange(xpChangeResult, member, guild, embedCache).ifPresent(it ->
                    settingsService.getBotChannel(guild).sendMessage(it).queue()
            );
        }

        if (reward.roleId() > 0) {
            guild.removeRoleFromMember(member, guild.getRoleById(reward.roleId())).queue();
        }

        var builder = new MessageCreateBuilder()
                .addContent(member.getAsMention())
                .addEmbeds(embedCache.getEmbed("karmaRewardRemove")
                        .injectValue("user", member.getAsMention())
                        .toEmbedBuilder()
                        .build()
                ).build();
        settingsService.getBotChannel(guild).sendMessage(builder).queue();
    }
}
