package de.kaktushose.levelbot.util;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class Pagination {

    private final Levelbot levelbot;
    private final CurrencyType currencyType;
    private final List<BotUser> botUsers;
    private final int pageSize; // elements per page
    private int index; // current page index

    public Pagination(Levelbot levelbot, int pageSize, CurrencyType currencyType) {
        this.levelbot = levelbot;
        this.currencyType = currencyType;
        this.pageSize = pageSize;
        this.index = 0;
        Database database = levelbot.getDatabase();
        switch (currencyType) {
            case XP:
                botUsers = database.getUsers().getXpLeaderboard();
                break;
            case COINS:
                botUsers = database.getUsers().getCoinsLeaderboard();
                break;
            case DIAMONDS:
                botUsers = database.getUsers().getDiamondsLeaderboard();
                break;
            default:
                throw new IllegalArgumentException("Unsupported currency type!");
        }
    }

    public void nextPage() {
        index++;
        if (index + 1 > pages()) {
            index--;
        }
    }

    public void previousPage() {
        index--;
        if (index < 0) {
            index = 0;
        }
    }

    public List<String> getPage() {
        int fromIndex = index == 0 ? 0 : index * pageSize + 1;
        int toIndex = index == 0 ? pageSize : index * pageSize + pageSize + 1;

        // end of list reached, set toIndex to list size
        if (toIndex > botUsers.size()) {
            toIndex = botUsers.size();
        }

        return botUsers.subList(fromIndex, toIndex).stream().map(this::format).collect(Collectors.toList());
    }



    public int size() {
        return botUsers.size();
    }

    public int index() {
        return index;
    }

    public int pages() {
        return (int) Math.ceil((float) botUsers.size() / (float) pageSize);
    }

    private String format(BotUser botUser) {
        User user = levelbot.getJda().getUserById(botUser.getUserId());
        switch (currencyType) {
            case XP:
                return String.format("%s#%s (%d XP)", user.getName(), user.getDiscriminator(), botUser.getXp());
            case COINS:
                return String.format("%s#%s (%d Münzen)", user.getName(), user.getDiscriminator(), botUser.getCoins());
            case DIAMONDS:
                return String.format("%s#%s (%d Diamanten)", user.getName(), user.getDiscriminator(), botUser.getDiamonds());
            default:
                throw new IllegalArgumentException("Unsupported currency type!");
        }
    }

    public enum CurrencyType {
        XP,
        COINS,
        DIAMONDS
    }

}
