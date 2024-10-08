package com.github.kaktushose.nplaybot.events.reactions.karma;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public class KarmaUpvoteEvent extends JDABotEvent<GenericMessageReactionEvent> {

    private final Message message;

    public KarmaUpvoteEvent(GenericMessageReactionEvent event, Bot bot, Message message) {
        super(event, bot);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

}
