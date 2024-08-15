package com.github.kaktushose.nplaybot.events.messages.delete.impl;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.messages.delete.GenericMessageDeletedEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;

public class StarboardEntryDeletedEvent extends GenericMessageDeletedEvent {

    public StarboardEntryDeletedEvent(MessageDeleteEvent event, Bot bot) {
        super(event, bot);
    }
}
