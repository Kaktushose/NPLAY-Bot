package com.github.kaktushose.nplaybot;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LegacyCommandListener extends ListenerAdapter {

    private final EmbedCache embedCache;

    public LegacyCommandListener(EmbedCache embedCache) {
        this.embedCache = embedCache;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (!event.isFromGuild()) {
            return;
        }
        if (!event.getMessage().getContentDisplay().startsWith("!")) {
            return;
        }
        event.getChannel().sendMessage(embedCache.getEmbed("legacyCommandInfo").toMessageCreateData()).queue(it ->
                it.delete().queueAfter(30, TimeUnit.SECONDS)
        );
    }

}
