package com.github.kaktushose.nplaybot.karma;

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

@Interaction
public class KarmaConfigCommands {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "add karma", desc = "Fügt einem User Karma hinzu", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    @Permissions(BotPermissions.MODIFY_USER_BALANCE)
    public void onAddKarma(CommandEvent event, Member target, @Min(Integer.MIN_VALUE) @Max(Integer.MAX_VALUE) int amount) {
        database.getKarmaService().addKarma(target, amount);

        event.reply(embedCache.getEmbed("addKarmaResult")
                .injectValue("user", target.getAsMention())
                .injectValue("karma", amount)
        );
    }

    @SlashCommand(value = "set karma", desc = "Setzt die Karma Punkte von einem User auf den angegebenen Wert", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    @Permissions(BotPermissions.MODIFY_USER_BALANCE)
    public void onSetKarma(CommandEvent event, Member target, @Min(Integer.MIN_VALUE) @Max(Integer.MAX_VALUE) int value) {
        database.getKarmaService().setKarma(target, value);

        event.reply(embedCache.getEmbed("setKarmaResult")
                .injectValue("user", target.getAsMention())
                .injectValue("karma", value)
        );
    }

    @SlashCommand(value = "set default karma-tokens", desc = "Legt die tägliche Anzahl an Karma-Tokens für jeden Nutzer fest", enabledFor = Permission.BAN_MEMBERS, isGuildOnly = true)
    @Permissions(BotPermissions.MANAGE_KARMA_SETTINGS)
    public void onSetKarmaTokens(CommandEvent event, @Min(1) @Max(Integer.MAX_VALUE) int value) {
        database.getKarmaService().setDefaultTokens(event.getGuild(), value);
        onGetKarmaConfig(event);
    }

    @SlashCommand(value = "get karma config", desc = "Zeigt die Einstellungen für das Karma System an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissions.MANAGE_KARMA_SETTINGS)
    public void onGetKarmaConfig(CommandEvent event) {
        var emojis = database.getKarmaService().getValidEmojis(event.getGuild());
        var builder = new StringBuilder();
        emojis.forEach(it -> builder.append(it.getFormatted()).append(" "));
        event.reply(embedCache.getEmbed("karmaConfig")
                        .injectValue("emojis", builder)
                .injectValue("tokens", database.getKarmaService().getDefaultTokens(event.getGuild()))
        );
    }
}
