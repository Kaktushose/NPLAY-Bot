package com.github.kaktushose.nplaybot.events.reactions.contest;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.reactions.GenericReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * This event fires for any reaction that gets added to a contest entry.
 */
public class ContestReactionAddEvent extends GenericReactionEvent {

    public ContestReactionAddEvent(MessageReactionAddEvent event, Bot bot) {
        super(event, bot);
    }

    public long getMessageAuthorId() {
        return ((MessageReactionAddEvent) getJDAEvent()).getMessageAuthorIdLong();
    }

}
