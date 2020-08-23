package de.kaktushose.nrtv.discord.frameworks.reactionwaiter;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReactionListener extends ListenerAdapter {

    private static List<ReactionWaiter> waiters = new CopyOnWriteArrayList<>();
    static void addReactionWaiter(ReactionWaiter waiter) {
        waiters.add(waiter);
    }

    static void removeReactionWaiter(ReactionWaiter waiter) {
        waiters.remove(waiter);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        waiters.forEach(waiter -> waiter.getEmotes().forEach(emote -> {
            if (!emote.name.equals(event.getReactionEmote().getName())) return;
            if (!(waiter.getMessageId() == 0 || waiter.getMessageId() == event.getMessageIdLong())) return;
            if (!(waiter.getMemberId() == 0 || waiter.getMemberId() == event.getMember().getIdLong())) return;
            waiter.called(new ReactionEvent(event, emote));
        }));
    }
}

