package com.github.kaktushose.nplaybot.events.messages.delete;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;

public abstract class GenericMessageDeletedEvent extends JDABotEvent<MessageDeleteEvent> {

    public GenericMessageDeletedEvent(MessageDeleteEvent event, Bot bot) {
        super(event, bot);
    }

    public Long getMessageId() {
        return getJDAEvent().getMessageIdLong();
    }

}
