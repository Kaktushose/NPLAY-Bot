package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Interaction
@Permissions(BotPermissions.MODIFY_RANK_SETTINGS)
public class RankConfigCommands {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "get rank config", desc = "Zeigt die Einstellungen für das Rank System an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onGetRankConfig(CommandEvent event) {
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig(event.getGuild())));
    }

    @SlashCommand(value = "set cooldown", desc = "Legt den Cooldown für gewertete Nachrichten fest", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onSetCooldown(CommandEvent event, @Param("Die Dauer in Millisekunden") @Min(0) @Max(Integer.MAX_VALUE) int cooldown) {
        database.getRankService().updateCooldown(event.getGuild(), cooldown);
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig(event.getGuild())));
    }

    @SlashCommand(value = "set message length", desc = "Legt die Mindestlänge für gewertete Nachrichten fest", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onSetMinMessageLength(CommandEvent event, @Param("Die Mindestanzahl an Buchstaben pro Nachricht") @Min(0) @Max(Integer.MAX_VALUE) int length) {
        database.getRankService().updateMinMessageLength(event.getGuild(), length);
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig(event.getGuild())));
    }

    @SlashCommand(value = "set xp-loot chance", desc = "Legt die Wahrscheinlichkeit für zufällige XP-Loot-Drops fest", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onSetXpLootDropChance(CommandEvent event, @Param("Die Wahrscheinlichkeit in Prozent") @Min(1) @Max(100) double chance) {
        database.getRankService().updateXpLootChance(event.getGuild(), chance);
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig(event.getGuild())));
    }

    @SlashCommand(value = "valid channels list", desc = "Zeigt die Textkanäle an, in denen Nachrichten gewertet werden", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onValidChannelsList(CommandEvent event) {
        var channels = database.getRankService().getValidChannels(event.getGuild());
        StringBuilder result = new StringBuilder();
        channels.forEach(it -> result.append(String.format("<#%d>", it)).append("\n"));
        event.reply(embedCache.getEmbed("validChannels").injectValue("channels", result));
    }

    @SlashCommand(value = "valid channels add", desc = "Fügt einen Textkanal zu der Liste der gewerteten Kanäle hinzu", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onValidChannelsAdd(CommandEvent event, @Param("Der Kanal der gewertet werden soll") TextChannel channel) {
        var channels = database.getRankService().getValidChannels(event.getGuild());
        channels.add(channel.getIdLong());
        database.getRankService().updateValidChannels(event.getGuild(), channels);
        StringBuilder result = new StringBuilder();
        channels.forEach(it -> result.append(String.format("<#%d>", it)).append("\n"));
        event.reply(embedCache.getEmbed("validChannels").injectValue("channels", result));
    }

    @SlashCommand(value = "valid channels remove", desc = "Entfernt einen Textkanal von der Liste der gewerteten Kanäle", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onValidChannelsRemove(CommandEvent event, @Param("Der Kanal der nicht mehr gewertet werden soll") TextChannel channel) {
        var channels = database.getRankService().getValidChannels(event.getGuild());
        channels.remove(channel.getIdLong());
        database.getRankService().updateValidChannels(event.getGuild(), channels);
        StringBuilder result = new StringBuilder();
        channels.forEach(it -> result.append(String.format("<#%d>", it)).append("\n"));
        event.reply(embedCache.getEmbed("validChannels").injectValue("channels", result));
    }
}
