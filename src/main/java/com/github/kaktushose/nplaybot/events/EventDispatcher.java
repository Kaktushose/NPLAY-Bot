package com.github.kaktushose.nplaybot.events;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.messages.valid.ValidMessageProducer;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDispatcher {

    private final Map<Class<? extends GenericBotEvent>, List<BotEventListener<? extends GenericBotEvent>>> listeners;
    private final Bot bot;

    public EventDispatcher(Bot bot) {
        this.bot = bot;
        listeners = new HashMap<>();
        registerJDAListeners(bot.getJda());
        registerBotListeners();
    }

    private void registerJDAListeners(JDA jda) {
        jda.addEventListener(new ValidMessageProducer(bot));
    }

    private void registerBotListeners() {

    }

    @SafeVarargs
    private void register(Class<? extends GenericBotEvent> event, BotEventListener<? extends GenericBotEvent>... listener) {
        if (!listeners.containsKey(event)) {
            listeners.put(event, new ArrayList<>());
        }
        for (var it : listener) {
            listeners.get(event).add(it);
        }
    }

    public void dispatch(GenericBotEvent event) {
        if (!listeners.containsKey(event.getClass())) {
            return;
        }
        listeners.get(event.getClass()).forEach(it -> it.onEventInternal(event));
    }

}
