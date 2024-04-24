package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Optional;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import net.dv8tion.jda.api.entities.Member;

@Interaction
@Permissions(BotPermissions.USER)
public class RankInfoCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "rank", isGuildOnly = true, desc = "Zeigt die Kontoinformationen zu einem User an")
    public void onCommand(CommandEvent event, @Optional Member member) {
        var target = member == null ? event.getMember() : member;
        UserInfo userInfo = database.getRankService().getUserInfo(target);

        var embed = userInfo.nextRank().isPresent() ? "rankInfo" : "rankInfoMax";
        event.reply(embedCache.getEmbed(embed).injectValues(userInfo.getEmbedValues(target)));
    }
}
