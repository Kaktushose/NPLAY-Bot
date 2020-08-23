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
                bot.getBotChannel().sendMessage(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle(String.format("Danke für Deine Hilfe, %s! :sunny:", bot.getGuild().getMember(user).getEffectiveName()))
                        .setDescription(String.format("Du hast nun bereits **%d Sonnen** gesammelt.\n" +
                                "Als Belohnung erhälst du die **Eventrolle SOMMER 2020** geschenkt.\nWeiterhin viel Spaß mit unseren Sommer-Events!", newPoints))
                        .build()).queue();
            }
        });
    }

    public void addEventReward(EventReward eventReward) {
        rewardMap.put(eventReward.getBound(), eventReward);
    }

}
