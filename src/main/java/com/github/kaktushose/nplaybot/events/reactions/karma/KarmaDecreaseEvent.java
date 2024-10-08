package com.github.kaktushose.nplaybot.events.reactions.karma;

public class KarmaDecreaseEvent extends KarmaBalanceChangeEvent {

    public KarmaDecreaseEvent(KarmaBalanceChangeEvent event) {
        super(event);
    }
}
