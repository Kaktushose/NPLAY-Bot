package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("DataFlowIssue")
public class RankListener extends ListenerAdapter {

    private final RankService rankService;
    private final EmbedCache embedCache;

    public RankListener(RankService rankService, EmbedCache embedCache) {
        this.rankService = rankService;
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
        updateRankRoles(event.getMember(), event.getGuild(), result);

        if (!result.rankChanged()) {
            return;
        }
        var embed = result.nextRank().isPresent() ? "rankIncrease" : "rankIncreaseMax";
        event.getChannel().sendMessage(
                embedCache.getEmbed(embed).injectValues(result.getEmbedValues(author)).toMessageCreateData()
        ).queue();
    }

    private void updateRankRoles(Member member, Guild guild, XpChangeResult result) {
        var validRole = guild.getRoleById(result.currentRank().roleId());
        var invalidRoles = rankService.getRankRoleIds().stream()
                .map(guild::getRoleById)
                .filter(it -> it != validRole)
                .toList();
        guild.modifyMemberRoles(member, List.of(validRole), invalidRoles).queue();
    }
}
