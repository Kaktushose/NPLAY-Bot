package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DailyRewardListener extends ListenerAdapter {

    // this should find it's way into the database one day as well
    public static final long DAILY_REWARD_MESSAGE_ID = 846777362166579241L;
    private final Levelbot levelbot;

    public DailyRewardListener(Levelbot levelbot) {
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        // bots should just be ignored
        if (event.getUser().isBot()) {
            return;
        }
        // must be in channel #levelsystem
        if (event.getChannel().getIdLong() != 839150041955565588L) {
            return;
        }
        // must be right message
        if (event.getMessageIdLong() != DAILY_REWARD_MESSAGE_ID) {
            return;
        }
        // must be :gift: emote
        if (!event.getReactionEmote().getName().equals("\uD83C\uDF81")) {
            return;
        }
        User user = event.getUser();
        Optional<String> reward = levelbot.getLevelService().getDailyReward(user.getIdLong());

        if (reward.isPresent()) {
            MessageBuilder builder = new MessageBuilder().append(user.getAsMention());
            builder.setEmbed(levelbot.getEmbedCache()
                    .getEmbed("dailyReward")
                    .injectValue("user", user.getName())
                    .injectValue("reward", reward.get())
                    .toMessageEmbed()
            );
            event.getChannel().sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }
}
