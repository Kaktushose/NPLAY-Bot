package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DailyRewardListener extends ListenerAdapter {

    // this should find its way into the database one day as well
    public static final long DAILY_REWARD_MESSAGE_ID = 851454384893067274L;
    private final Levelbot levelbot;

    public DailyRewardListener(Levelbot levelbot) {
        this.levelbot = levelbot;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        // bots should just be ignored
        if (event.getUser().isBot()) {
            return;
        }
        // must be right message
        if (event.getMessageIdLong() != DAILY_REWARD_MESSAGE_ID) {
            return;
        }

        if (levelbot.getUserService().getMutedUsers().contains(event.getUser().getIdLong())) {
            return;
        }

        // must be :gift: emote
        if (!event.getEmoji().getName().equals("\uD83C\uDF81")) {
            return;
        }
        User user = event.getUser();
        Optional<String> reward = levelbot.getLevelService().getDailyReward(user.getIdLong());
        MessageBuilder builder = new MessageBuilder().append(user.getAsMention());
        if (reward.isPresent()) {
            builder.setEmbeds(levelbot.getEmbedCache()
                    .getEmbed("dailyReward")
                    .injectValue("user", user.getName())
                    .injectValue("reward", reward.get())
                    .toMessageEmbed()
            );
            event.getChannel().sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        } else {
            long timePassed = System.currentTimeMillis() - levelbot.getUserService().getUserById(user.getIdLong()).getLastReward();
            long millis = TimeUnit.HOURS.toMillis(24) - timePassed;
            long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
            builder.setEmbeds(levelbot.getEmbedCache()
                    .getEmbed("rewardAlreadyClaimed")
                    .injectValue("hours", hours)
                    .toMessageEmbed()
            );
            event.getChannel().sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }
}
