package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Interaction
@Permissions(BotPermissions.MANAGE_RANK_SETTINGS)
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class RankConfigCommands {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(value = "rank-config display", desc = "Zeigt die Einstellungen für das Rank System an")
    public void onGetRankConfig(CommandEvent event) {
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig()));
    }

    @Command(value = "rank-config set cooldown", desc = "Legt den Cooldown für gewertete Nachrichten fest")
    public void onSetCooldown(CommandEvent event, @Param("Die Dauer in Millisekunden") @Min(0) @Max(Integer.MAX_VALUE) Integer cooldown) {
        database.getRankService().updateCooldown(cooldown);
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig()));
    }

    @Command(value = "rank-config set message-length", desc = "Legt die Mindestlänge für gewertete Nachrichten fest")
    public void onSetMinMessageLength(CommandEvent event, @Param("Die Mindestanzahl an Buchstaben pro Nachricht") @Min(0) @Max(Integer.MAX_VALUE) Integer length) {
        database.getRankService().updateMinMessageLength(length);
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig()));
    }

    @Command(value = "rank-config set xp-loot-chance", desc = "Legt die Wahrscheinlichkeit für zufällige XP-Loot-Drops fest")
    public void onSetXpLootDropChance(CommandEvent event, @Param("Die Wahrscheinlichkeit in Prozent") @Min(1) @Max(100) Double chance) {
        database.getRankService().updateXpLootChance(chance);
        event.reply(embedCache.getEmbed("rankConfig").injectFields(database.getRankService().getRankConfig()));
    }

    @Command(value = "rank-config valid-channels list", desc = "Zeigt die Textkanäle an, in denen Nachrichten gewertet werden")
    public void onValidChannelsList(CommandEvent event) {
        var channels = database.getRankService().getValidChannels();
        StringBuilder result = new StringBuilder();
        channels.forEach(it -> result.append(String.format("<#%d>", it)).append("\n"));
        event.reply(embedCache.getEmbed("validChannels").injectValue("channels", result));
    }

    @Command(value = "rank-config valid-channels add", desc = "Fügt einen Textkanal zu der Liste der gewerteten Kanäle hinzu")
    public void onValidChannelsAdd(CommandEvent event, @Param("Der Kanal der gewertet werden soll") TextChannel channel) {
        var channels = database.getRankService().getValidChannels();
        channels.add(channel.getIdLong());
        database.getRankService().updateValidChannels(channels);
        StringBuilder result = new StringBuilder();
        channels.forEach(it -> result.append(String.format("<#%d>", it)).append("\n"));
        event.reply(embedCache.getEmbed("validChannels").injectValue("channels", result));
    }

    @Command(value = "rank-config valid-channels remove", desc = "Entfernt einen Textkanal von der Liste der gewerteten Kanäle")
    public void onValidChannelsRemove(CommandEvent event, @Param("Der Kanal der nicht mehr gewertet werden soll") TextChannel channel) {
        var channels = database.getRankService().getValidChannels();
        channels.remove(channel.getIdLong());
        database.getRankService().updateValidChannels(channels);
        StringBuilder result = new StringBuilder();
        channels.forEach(it -> result.append(String.format("<#%d>", it)).append("\n"));
        event.reply(embedCache.getEmbed("validChannels").injectValue("channels", result));
    }
}
