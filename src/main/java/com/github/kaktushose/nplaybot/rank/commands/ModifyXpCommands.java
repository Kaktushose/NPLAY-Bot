package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction(ephemeral = true)
public class ModifyXpCommands {

    private static final Logger log = LoggerFactory.getLogger(ModifyXpCommands.class);

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "add xp", desc = "Fügt einem User XP hinzu", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    public void onAddXp(CommandEvent event, Member target, @Min(1) @Max(Integer.MAX_VALUE) int amount) {
        var result = database.getRankService().addXp(target, amount);

        event.reply(embedCache.getEmbed("addXpResult")
                .injectValue("user", target.getAsMention())
                .injectValue("xp", amount)
        );

        checkRankUpdate(result, target, event.getGuild());
    }

    @SlashCommand(value = "set xp", desc = "Setzt die XP von einem User auf den angegebenen Wert", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    public void onSetXp(CommandEvent event, Member target, @Min(0) @Max(Integer.MAX_VALUE) int value) {
        var result = database.getRankService().setXp(target, value);

        event.reply(embedCache.getEmbed("setXpResult")
                .injectValue("user", target.getAsMention())
                .injectValue("xp", value)
        );

        checkRankUpdate(result, target, event.getGuild());
    }

    private void checkRankUpdate(XpChangeResult result, Member member, Guild guild) {
        log.debug("Checking for rank up: {}", member);
        database.getRankService().updateRankRoles(member, guild, result);

        if (!result.rankChanged()) {
            log.debug("Rank hasn't changed");
            return;
        }
        log.debug("Applying changes. New rank: {}", result.currentRank());

        var embed = result.nextRank().isPresent() ? "rankIncrease" : "rankIncreaseMax";
        var messageData = new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(embedCache.getEmbed(embed).injectValues(result.getEmbedValues(member.getUser())).toMessageEmbed())
                .build();
        database.getSettingsService().getBotChannel(guild).sendMessage(messageData).queue();
    }
}
