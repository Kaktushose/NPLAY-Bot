package com.github.kaktushose.nplaybot.events.messages.receive.impl;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.messages.receive.GenericMessageReceivedEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GuildMessageEvent extends GenericMessageReceivedEvent {

    public GuildMessageEvent(MessageReceivedEvent event, Bot bot, Member member) {
        super(event, bot, member);
    }
}
