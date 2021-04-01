package de.kaktushose.nrtv.discord.frameworks.event;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EventScheduler {

    private Map<Integer, EventReward> rewardMap;
    private Bot bot;

    public EventScheduler(Bot bot) {
        this.bot = bot;
        rewardMap = new HashMap<>();
    }

    public void onEventPointAdd(BotUser botUser, User user) {
        int newPoints = botUser.getEventPoints() + 1;
        botUser.setEventPoints(newPoints);
        rewardMap.forEach((bound, eventReward) -> {
            if (newPoints == bound) {
                eventReward.onReward(botUser, bot);
                bot.getBotChannel().sendMessage(user.getAsMention()).queue();
                if (newPoints == 3) {
                    bot.getBotChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle(String.format("Danke für deine Hilfe, %s! :rabbit:", bot.getGuild().getMember(user).getEffectiveName()))
                            .setDescription("Du hast nun bereits 3 Ostereier gesammelt.\nAls Belohnung hat Dir der Osterhase die **Eventrolle OSTERN 2021** geschenkt!")
                            .build()).queue();
                } else {
                    bot.getBotChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle(String.format("Danke für deine Hilfe, %s! :rabbit:", bot.getGuild().getMember(user).getEffectiveName()))
                            .setDescription("Das macht dir so schnell keiner nach: Du hast nun unglaubliche **25 Ostereier** gesammelt.\n" +
                                   "Als Belohnung hat Dir der Osterhase **PREMIUM basic** geschenkt.")
                            .build()).queue();
                }

            }
        });
    }

    public void addEventReward(EventReward eventReward) {
        rewardMap.put(eventReward.getBound(), eventReward);
    }

}
