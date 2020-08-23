package de.kaktushose.nrtv.discord.core.bot.listeners;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;
import de.kaktushose.nrtv.discord.frameworks.reactionwaiter.EmoteType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Date;

public class LevelListener extends ListenerAdapter {

    private final Bot bot;

    public LevelListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        if (event.getChannel().getIdLong() == 743447345592795226L) {
            event.getMessage().addReaction("⭐").queue();
        }

        Date date = new Date();

        if (bot.getDatabase().getMutedChannelIds().contains(event.getChannel().getIdLong())) {
            return;
        }
        if (event.getAuthor().isBot()) {
            return;
        }
        if (Arrays.stream(new String[]{"!", "-", "?", "."}).anyMatch(event.getMessage().getContentRaw()::startsWith)) {
            return;
        }

        BotUser botUser = bot.getDatabase().getBotUser(event.getMember().getIdLong());

        if (!bot.hasPermission(event.getMember(), PermissionLevel.MEMBER)) {
            return;
        }
        if (date.getTime() - botUser.getLastXp() < 1200000L) {
            return;
        }

        String message = event.getMessage().getContentStripped();
        message = message.toLowerCase().replaceAll(":(.|\\n)+?:|\\p{C}|@(.*?)\\s|(?:\\s|^)#[A-Za-z0-9\\-._]+(?:\\s|$)|\\s+", "");
        if (message.length() >= 10) {
            botUser.setLastXp(System.currentTimeMillis());

            if (botUser.hasItem(ItemType.BOOSTER)) {
                botUser.setCoins(botUser.getCoins() + bot.getNewCoins() + 2);
            } else {
                botUser.setCoins(botUser.getCoins() + bot.getNewCoins());
            }
            if (botUser.hasItem(ItemType.XPBOOSTER)) {
                botUser.setXp(botUser.getXp() + bot.getNewXp() + 2);
            } else {
                botUser.setXp(botUser.getXp() + bot.getNewXp());
            }

            botUser.setDiamonds(botUser.getDiamonds() + bot.getNewDiamonds());

            botUser.setMessages(botUser.getMessages() + 1);
            if (bot.eventIsPresent()) {
                bot.getEventScheduler().onEventPointAdd(botUser, event.getAuthor());
            }
            bot.checkForPromotion(botUser, event);
        }
    }
}
