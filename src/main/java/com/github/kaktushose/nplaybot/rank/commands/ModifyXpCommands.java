package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.CommandConfig;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.google.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
@Permissions(BotPermissions.MODIFY_USER_BALANCE)
public class ModifyXpCommands {

    private static final Logger log = LoggerFactory.getLogger(ModifyXpCommands.class);

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(value = "balance add xp", desc = "Fügt einem User XP hinzu")
    public void onAddXp(CommandEvent event, Member target, @Min(1) @Max(Integer.MAX_VALUE) Integer amount) {
        var result = database.getRankService().addXp(target, amount);

        event.with().ephemeral(true).reply(embedCache.getEmbed("addXpResult")
                .injectValue("user", target.getAsMention())
                .injectValue("xp", amount)
        );

        database.getRankService().onXpChange(result, target, embedCache);
    }

    @Command(value = "balance set xp", desc = "Setzt die XP von einem User auf den angegebenen Wert")
    public void onSetXp(CommandEvent event, Member target, @Min(0) @Max(Integer.MAX_VALUE) Integer value) {
        var result = database.getRankService().setXp(target, value);

        event.with().ephemeral(true).reply(embedCache.getEmbed("setXpResult")
                .injectValue("user", target.getAsMention())
                .injectValue("xp", value)
        );

        database.getRankService().onXpChange(result, event.getMember(), embedCache);
    }
}
