package com.github.kaktushose.nplaybot.events.reactions.karma;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.GenericBotEvent;
import net.dv8tion.jda.api.entities.Member;

public class KarmaBalanceChangeEvent extends GenericBotEvent {

    private final int oldKarma;
    private final int newKarma;
    private final Member member;

    public KarmaBalanceChangeEvent(Bot bot, int oldKarma, int newKarma, Member member) {
        super(bot);
        this.oldKarma = oldKarma;
        this.newKarma = newKarma;
        this.member = member;
    }

    public KarmaBalanceChangeEvent(KarmaBalanceChangeEvent event) {
        this(event.getBot(), event.getOldKarma(), event.getNewKarma(), event.getMember());
    }

    public int getOldKarma() {
        return oldKarma;
    }

    public int getNewKarma() {
        return newKarma;
    }

    public Member getMember() {
        return member;
    }
}
