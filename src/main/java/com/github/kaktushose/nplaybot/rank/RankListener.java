package com.github.kaktushose.nplaybot.rank;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class RankListener extends ListenerAdapter {

    private final RankService rankService;

    public RankListener(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var author = event.getAuthor();
        var message = event.getMessage();

        if (author.isBot()) {
            return;
        }

        event.getChannel().sendMessage("valid message: " + rankService.isValidMessage(message)).queue();
    }
}
