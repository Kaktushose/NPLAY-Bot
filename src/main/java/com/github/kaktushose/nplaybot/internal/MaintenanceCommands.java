package com.github.kaktushose.nplaybot.internal;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import net.dv8tion.jda.api.Permission;

@Interaction
public class MaintenanceCommands {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "reload embeds", desc = "Aktualisiert den EmbedCache", enabledFor = Permission.BAN_MEMBERS)
    public void onReload(CommandEvent event) {
        embedCache.loadEmbeds();
        event.reply(embedCache.getEmbed("embedCacheReload"));
    }

}
