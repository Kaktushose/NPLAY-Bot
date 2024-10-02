package com.github.kaktushose.nplaybot.events.reactions;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.util.function.Consumer;

public class GenericReactionEvent extends JDABotEvent<GenericMessageReactionEvent> {

    public GenericReactionEvent(GenericMessageReactionEvent event, Bot bot) {
        super(event, bot);
    }

    public void withMessage(Consumer<Message> message) {
        getJDAEvent().retrieveMessage().queue(message);
    }

    public User getUser() {
        return getJDAEvent().getUser();
    }

    public long getUserId() {
        return getUser().getIdLong();
    }

    public Emoji getEmoji() {
        return getJDAEvent().getEmoji();
    }

    public long getMessageId() {
        return getJDAEvent().getMessageIdLong();
    }
}
