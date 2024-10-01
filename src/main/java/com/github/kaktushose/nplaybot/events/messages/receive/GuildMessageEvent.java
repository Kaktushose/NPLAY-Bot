package com.github.kaktushose.nplaybot.events.messages.receive;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.JDABotEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GuildMessageEvent extends JDABotEvent<MessageReceivedEvent> {

    private final Member member;

    public GuildMessageEvent(MessageReceivedEvent event, Bot bot, Member member) {
        super(event, bot);
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    public Message getMessage() {
        return getJDAEvent().getMessage();
    }

    public TextChannel getChannel() {
        return getJDAEvent().getChannel().asTextChannel();
    }
}
