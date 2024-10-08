package com.github.kaktushose.nplaybot.events.reactions.starboard;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

/**
 * Indicates that a message received enough votes to be posted in the starboard. This event fires regardless of an entry
 * already being posted or not.
 */
public class StarboardPostUpdateEvent extends JDABotEvent<GenericMessageReactionEvent> {

    private final int voteCount;
    private final Message message;

    public StarboardPostUpdateEvent(GenericMessageReactionEvent event, Bot bot, int voteCount, Message message) {
        super(event, bot);
        this.voteCount = voteCount;
        this.message = message;
    }

    public Member getMember() {
        return getJDAEvent().getMember();
    }

    public Guild getGuild() {
        return getJDAEvent().getGuild();
    }

    public Message getMessage() {
        return message;
    }

    public int getVoteCount() {
        return voteCount;
    }
}
