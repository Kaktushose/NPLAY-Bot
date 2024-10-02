package com.github.kaktushose.nplaybot.events.reactions.contest;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.function.Consumer;

/**
 * Indicates that all vote emojis got removed from a contest entry by a moderator.
 * This will not fire a {@link ContestVoteRemoveEvent}.
 */
public class ContestReactionRemoveEvent extends JDABotEvent<GenericMessageEvent> {

    public ContestReactionRemoveEvent(GenericMessageEvent event, Bot bot) {
        super(event, bot);
    }

    public void withMessage(Consumer<Message> message) {
        getJDAEvent().getChannel().retrieveMessageById(getJDAEvent().getMessageId()).queue(message);
    }
}
