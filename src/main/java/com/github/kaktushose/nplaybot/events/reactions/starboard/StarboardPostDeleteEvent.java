package com.github.kaktushose.nplaybot.events.reactions.starboard;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

/**
 * Indicates that a starboard post should be deleted
 */
public class StarboardPostDeleteEvent extends JDABotEvent<GenericMessageEvent> {

    public StarboardPostDeleteEvent(GenericMessageEvent event, Bot bot) {
        super(event, bot);
    }

    public long getMessageId() {
        return getJDAEvent().getMessageIdLong();
    }
}
