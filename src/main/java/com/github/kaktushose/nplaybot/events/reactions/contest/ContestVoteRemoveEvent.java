package com.github.kaktushose.nplaybot.events.reactions.contest;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.reactions.GenericReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

/**
 * This event fires if and only if a vote from a contest entry gets removed, it does not fire if an arbitrary reaction
 * gets removed from a contest entry or a moderator removes all emojis.
 *
 */
public class ContestVoteRemoveEvent extends GenericReactionEvent {

    public ContestVoteRemoveEvent(MessageReactionRemoveEvent event, Bot bot) {
        super(event, bot);
    }
}
