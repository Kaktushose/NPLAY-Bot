package com.github.kaktushose.nplaybot.events.collect;

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

@Interaction
@Permissions(BotPermissions.MANAGE_EVENTS)
public class CollectEventCommands {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;

    @SlashCommand(value = "collect event start", desc = "Startet ein Collect Event", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onCollectEventStart(CommandEvent event,
                                    @Param("Der Name des Events") String eventName,
                                    @Param("Der Name der Währung die gesammelt werden soll, z.B. \"Schneemänner\"") String currencyName,
                                    @Param("Die Emoji-Repräsentation der Währung, die gesammelt werden soll") String emoji) {
        if (database.getCollectEventService().isActive(event.getGuild())) {
            event.reply(embedCache.getEmbed("collectEventStartError"));
            return;
        }
        database.getCollectEventService().startCollectEvent(event.getGuild(), eventName, currencyName, emoji);
        event.reply(embedCache.getEmbed("collectEventStart").injectValue("name", eventName));
    }

    @SlashCommand(value = "collect event stop", desc = "Stoppt das aktuelle Collect Event", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onCollectEventStop(CommandEvent event) {
        database.getCollectEventService().stopCollectEvent(event.getGuild());
        event.reply(embedCache.getEmbed("collectEventStop"));
    }

    @SlashCommand(value = "set collect-loot chance", desc = "Legt die Wahrscheinlichkeit für zufällige Collect-Loot-Drops fest", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void onSetXpLootDropChance(CommandEvent event, @Param("Die Wahrscheinlichkeit in Prozent") @Min(1) @Max(100) double chance) {
        database.getCollectEventService().updateCollectLootChance(event.getGuild(), chance);
        event.reply(embedCache.getEmbed("collectLootChanceUpdate").injectValue("chance", chance));
    }

}
