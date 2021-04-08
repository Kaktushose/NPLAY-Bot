package de.kaktushose.levelbot.util;

import de.kaktushose.levelbot.database.model.BotUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class Pagination {

    private final CurrencyType currencyType;
    private final JDA jda;
    private final List<BotUser> leaderboard;
    private final int pageSize; // elements per page
    private int index; // current page index

    public Pagination(int pageSize, List<BotUser> leaderboard, JDA jda, CurrencyType currencyType) {
        this.pageSize = pageSize;
        this.leaderboard = leaderboard;
        this.jda = jda;
        this.currencyType = currencyType;
        this.index = 0;

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
        if (toIndex > leaderboard.size()) {
            toIndex = leaderboard.size();
        }

        return leaderboard.subList(fromIndex, toIndex).stream().map(this::format).collect(Collectors.toList());
    }

    public int size() {
        return leaderboard.size();
    }

    public int index() {
        return index;
    }

    public int pages() {
        return (int) Math.ceil((float) leaderboard.size() / (float) pageSize);
    }

    private String format(BotUser botUser) {
        User user = jda.getUserById(botUser.getUserId());
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