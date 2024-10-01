package com.github.kaktushose.nplaybot.events.messages.receive;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.messages.receive.impl.ContestMessageEvent;
import com.github.kaktushose.nplaybot.events.messages.receive.impl.LegacyCommandEvent;
import com.github.kaktushose.nplaybot.events.messages.receive.impl.ValidMessageEvent;
import com.github.kaktushose.nplaybot.features.events.contest.ContestEventService;
import com.github.kaktushose.nplaybot.features.rank.RankService;
import com.github.kaktushose.nplaybot.features.settings.SettingsService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageReceiveListener extends ListenerAdapter {

    private final Bot bot;
    private final RankService rankService;
    private final PermissionsService permissionsService;
    private final SettingsService settingsService;
    private final ContestEventService contestEventService;

    public MessageReceiveListener(Bot bot) {
        this.bot = bot;
        rankService = bot.getDatabase().getRankService();
        permissionsService = bot.getDatabase().getPermissionsService();
        settingsService = bot.getDatabase().getSettingsService();
        contestEventService = bot.getDatabase().getContestEventService();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var dispatcher = bot.getEventDispatcher();
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
        var member = event.getMember();


        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }

        dispatcher.dispatch(new GuildMessageEvent(event, bot, member));

        if (event.getMessage().getChannelId().equals(settingsService.getBotChannel().getId())) {
            if (!event.getMessage().getContentDisplay().startsWith("!")) {
                return;
            }
            dispatcher.dispatch(new LegacyCommandEvent(event, bot, member));
        }

        if (event.getChannel().getIdLong() == contestEventService.getContestEventChannel()) {
            dispatcher.dispatch(new ContestMessageEvent(event, bot, member));
        }


        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }
        if (!rankService.isValidMessage(event.getMessage())) {
            return;
        }

        dispatcher.dispatch(new ValidMessageEvent(event, bot, member));
    }
}
