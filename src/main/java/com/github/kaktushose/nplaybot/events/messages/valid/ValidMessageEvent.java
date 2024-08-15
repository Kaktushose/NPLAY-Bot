package com.github.kaktushose.nplaybot.events.messages.valid;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ValidMessageEvent extends JDABotEvent<MessageReceivedEvent> {

    public ValidMessageEvent(MessageReceivedEvent event, Bot bot, Member member) {
        super(event, bot, member);
    }

}
