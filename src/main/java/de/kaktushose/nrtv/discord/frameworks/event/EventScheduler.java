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
                if (newPoints == 5) {
                    bot.getBotChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle(String.format("**Du bist wirklich fleißig**, %s! :christmas_tree:", bot.getGuild().getMember(user).getEffectiveName()))
                            .setDescription("Du hast bereits 5 Weihnachtsbäume geschmückt.\nDafür schenken wir dir die streng limitierte **Eventrolle XMAS 2020** :santa:, mit der du dich während der Festtage an prominenter Stelle in der Userliste wiederfinden kannst!\n" +
                                    "Frohe Weihnachten! :gift:\n\nÜbrigens: Einige Familien haben dieses Jahr noch keinen Weihnachtsbaum. Wenn du ihnen helfen willst, erhältst du **ab 25 geschmückten Bäumen** eine weitere **tolle Belohnung!**")
                            .build()).queue();
                } else {
                    bot.getBotChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle(String.format("**Überall Glocken und Lichter,** %s! :christmas_tree:", bot.getGuild().getMember(user).getEffectiveName()))
                            .setDescription("Das macht dir so schnell keiner nach: Du hast nun unglaubliche **25 Weihnachtsbäume** geschmückt. \n" +
                                    "Wie versprochen, erhältst du dafür eine ganz besondere Belohnung: Das **Special Item \"Christmas Booster\"** :santa: ! Erhalte ab sofort **7 Tage :alarm_clock:  lang** doppelte XP :star2: , Münzen :moneybag:  und Diamanten :gem: bei jeder gezählten Nachricht!\n" +
                                    "Frohe Weihnachten! :gift: ")
                            .build()).queue();
                }

            }
        });
    }

    public void addEventReward(EventReward eventReward) {
        rewardMap.put(eventReward.getBound(), eventReward);
    }

}
