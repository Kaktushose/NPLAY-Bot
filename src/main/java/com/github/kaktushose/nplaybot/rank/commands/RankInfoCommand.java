package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;

@Interaction
@Permissions(BotPermissions.USER)
public class RankInfoCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "rank", isGuildOnly = true, desc = "Zeigt die Kontoinformationen zu einem User an")
    public void onRankInfo(CommandEvent event, @Optional Member member) {
        var target = member == null ? event.getMember() : member;
        UserInfo userInfo = database.getRankService().getUserInfo(target);

        var embed = userInfo.nextRank().isPresent() ? "rankInfo" : "rankInfoMax";
        event.reply(embedCache.getEmbed(embed).injectValues(userInfo.getEmbedValues(target)));
    }

    @ContextCommand(value = "Kontoinformation abrufen", type = Command.Type.USER, isGuildOnly = true, ephemeral = true)
    public void onContextRankInfo(CommandEvent event, User user) {
        UserInfo userInfo = database.getRankService().getUserInfo(user);

        var embed = userInfo.nextRank().isPresent() ? "rankInfo" : "rankInfoMax";
        event.reply(embedCache.getEmbed(embed).injectValues(userInfo.getEmbedValues(user)));
    }
}
