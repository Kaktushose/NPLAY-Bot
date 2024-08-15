package com.github.kaktushose.nplaybot.features.rank.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction(ephemeral = true)
@Permissions(BotPermissions.MODIFY_USER_BALANCE)
public class ModifyXpCommands {

    private static final Logger log = LoggerFactory.getLogger(ModifyXpCommands.class);

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "balance add xp", desc = "Fügt einem User XP hinzu", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    public void onAddXp(CommandEvent event, Member target, @Min(1) @Max(Integer.MAX_VALUE) int amount) {
        var result = database.getRankService().addXp(target, amount);

        event.reply(embedCache.getEmbed("addXpResult")
                .injectValue("user", target.getAsMention())
                .injectValue("xp", amount)
        );

        database.getRankService().onXpChange(result, event.getMember(), embedCache);
    }

    @SlashCommand(value = "balance set xp", desc = "Setzt die XP von einem User auf den angegebenen Wert", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    public void onSetXp(CommandEvent event, Member target, @Min(0) @Max(Integer.MAX_VALUE) int value) {
        var result = database.getRankService().setXp(target, value);

        event.reply(embedCache.getEmbed("setXpResult")
                .injectValue("user", target.getAsMention())
                .injectValue("xp", value)
        );

        database.getRankService().onXpChange(result, event.getMember(), embedCache);
    }
}
