package com.github.kaktushose.nplaybot.events.messages.receive;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class GenericMessageReceivedEvent extends JDABotEvent<MessageReceivedEvent> {


    private final Member member;

    public GenericMessageReceivedEvent(MessageReceivedEvent event, Bot bot, Member member) {
        super(event, bot);
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    public Message getMessage() {
        return getJDAEvent().getMessage();
    }

}
