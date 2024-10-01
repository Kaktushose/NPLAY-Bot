package com.github.kaktushose.nplaybot.features;

import com.github.kaktushose.nplaybot.events.BotEvent;
import com.github.kaktushose.nplaybot.events.messages.receive.impl.LegacyCommandEvent;

import java.util.concurrent.TimeUnit;

public class LegacyCommandListener {

    @BotEvent
    public void onMessageReceived(LegacyCommandEvent event) {
        event.getChannel().sendMessage(event.getEmbedCache().getEmbed("legacyCommandInfo").toMessageCreateData()).queue(it ->
                it.delete().queueAfter(30, TimeUnit.SECONDS)
        );
    }
}
