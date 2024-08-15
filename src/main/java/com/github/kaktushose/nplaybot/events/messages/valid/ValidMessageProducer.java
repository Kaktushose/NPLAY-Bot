package com.github.kaktushose.nplaybot.events.messages.valid;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.features.rank.RankService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ValidMessageProducer extends ListenerAdapter {

    private final Bot bot;
    private final RankService rankService;
    private final PermissionsService permissionsService;

    public ValidMessageProducer(Bot bot) {
        this.bot = bot;
        rankService = bot.getDatabase().getRankService();
        permissionsService = bot.getDatabase().getPermissionsService();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var author = event.getAuthor();

        if (author.isBot()) {
            return;
        }
        if (!event.isFromGuild()) {
            return;
        }
        if (event.isWebhookMessage()) {
            return;
        }
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }
        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }
        if (!rankService.isValidMessage(event.getMessage())) {
            return;
        }

        bot.getEventDispatcher().dispatch(new ValidMessageEvent(event, bot, event.getMember()));
    }
}
