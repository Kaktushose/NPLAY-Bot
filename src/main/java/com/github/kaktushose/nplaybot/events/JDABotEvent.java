package com.github.kaktushose.nplaybot.events;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;

public abstract class JDABotEvent<T extends GenericEvent> extends GenericBotEvent {

    private final T event;

    public JDABotEvent(T event, Bot bot, Member member) {
        super(bot, member);
        this.event = event;
    }

    public T getJDAEvent() {
        return event;
    }
}
