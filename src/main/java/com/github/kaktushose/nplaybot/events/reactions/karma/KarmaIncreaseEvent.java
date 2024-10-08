package com.github.kaktushose.nplaybot.events.reactions.karma;

public class KarmaIncreaseEvent extends KarmaBalanceChangeEvent {

    public KarmaIncreaseEvent(KarmaBalanceChangeEvent event) {
        super(event);
    }
}
