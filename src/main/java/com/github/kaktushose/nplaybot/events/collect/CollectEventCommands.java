package com.github.kaktushose.nplaybot.events.collect;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;

@Interaction
@Permissions(BotPermissions.MANAGE_EVENTS)
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class CollectEventCommands {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;

    @Command(value = "events collect-event start", desc = "Startet ein Collect Event")
    public void onCollectEventStart(CommandEvent event,
                                    @Param("Der Name des Events") String eventName,
                                    @Param("Der Name der Währung die gesammelt werden soll, z.B. \"Schneemänner\"") String currencyName,
                                    @Param("Die Emoji-Repräsentation der Währung, die gesammelt werden soll") String emoji) {
        if (database.getCollectEventService().isCollectEventActive()) {
            event.reply(embedCache.getEmbed("collectEventStartError"));
            return;
        }
        database.getCollectEventService().startCollectEvent(eventName, currencyName, emoji);
        event.reply(embedCache.getEmbed("collectEventStart").injectValue("name", eventName));
    }

    @Command(value = "events collect-event stop", desc = "Stoppt das aktuelle Collect Event")
    public void onCollectEventStop(CommandEvent event) {
        database.getCollectEventService().stopCollectEvent();
        event.reply(embedCache.getEmbed("collectEventStop"));
    }

    @Command(value = "events set collect-loot-chance", desc = "Legt die Wahrscheinlichkeit für zufällige Collect-Loot-Drops fest")
    public void onSetXpLootDropChance(CommandEvent event, @Param("Die Wahrscheinlichkeit in Prozent") @Min(1) @Max(100) Double chance) {
        database.getCollectEventService().updateCollectLootChance(chance);
        event.reply(embedCache.getEmbed("collectLootChanceUpdate").injectValue("chance", chance));
    }

}
