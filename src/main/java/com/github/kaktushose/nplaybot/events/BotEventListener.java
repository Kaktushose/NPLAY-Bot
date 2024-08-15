package com.github.kaktushose.nplaybot.events;

public abstract class BotEventListener<T extends GenericBotEvent> {

    void onEventInternal(GenericBotEvent event) {
        onEvent((T) event);
    }

    public  abstract void onEvent(T event);

}
