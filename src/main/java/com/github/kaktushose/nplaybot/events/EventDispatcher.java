package com.github.kaktushose.nplaybot.events;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.messages.receive.MessageReceiveListener;
import com.github.kaktushose.nplaybot.events.reactions.MessageReactionListener;
import com.github.kaktushose.nplaybot.features.LegacyCommandListener;
import com.github.kaktushose.nplaybot.features.events.contest.ContestListener;
import net.dv8tion.jda.api.JDA;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventDispatcher {

    private final Map<Class<?>, Object> instances;
    private final Map<Class<? extends GenericBotEvent>, List<Method>> listeners;
    private final Bot bot;

    public EventDispatcher(Bot bot) {
        this.bot = bot;
        listeners = new HashMap<>();
        instances = new HashMap<>();
        registerListeners(bot.getJda());
    }

    private void registerListeners(JDA jda) {
        jda.addEventListener(new MessageReceiveListener(bot));
        jda.addEventListener(new MessageReactionListener(bot));

        addEventListener(new LegacyCommandListener());
        addEventListener(new ContestListener(bot));
    }

    @SuppressWarnings("unchecked")
    private void addEventListener(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(BotEvent.class)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("BotEvent method signatures must only have one event");
            }
            var type = method.getParameters()[0].getType();
            if (!GenericBotEvent.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Unknown event type: " + type.getName());
            }
            if (listeners.containsKey(type)) {
                listeners.get(type).add(method);
            } else {
                listeners.put((Class<? extends GenericBotEvent>) type, new ArrayList<>() {{
                    add(method);
                }});
            }
        }
        instances.put(listener.getClass(), listener);
    }

    public void dispatch(GenericBotEvent event) {
        if (!listeners.containsKey(event.getClass())) {
            return;
        }
        listeners.get(event.getClass()).forEach(method -> {
            try {
                method.invoke(instances.get(method.getDeclaringClass()), event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Event dispatching failed", e);
            }
        });
    }
}
