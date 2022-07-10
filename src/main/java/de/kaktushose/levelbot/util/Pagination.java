package de.kaktushose.levelbot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class Pagination {

    private final CurrencyType currencyType;
    private final JDA jda;
    private final List<? extends Pageable> leaderboard;
    private final int pageSize; // elements per page
    private int index; // current page index

    public Pagination(int pageSize, List<? extends Pageable> leaderboard, JDA jda, CurrencyType currencyType) {
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
        int fromIndex = index == 0 ? 0 : index * pageSize;
        int toIndex = index == 0 ? pageSize : index * pageSize + pageSize;

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

    private String format(Pageable pageable) {
        User user = jda.retrieveUserById(pageable.getUserId()).complete();
        return switch (currencyType) {
            case XP -> String.format("%s#%s (%d XP)", user.getName(), user.getDiscriminator(), pageable.getCount(CurrencyType.XP));
            case COINS -> String.format("%s#%s (%d Münzen)", user.getName(), user.getDiscriminator(), pageable.getCount(CurrencyType.COINS));
            case DIAMONDS -> String.format("%s#%s (%d Diamanten)", user.getName(), user.getDiscriminator(), pageable.getCount(CurrencyType.DIAMONDS));
            case CONTEST -> String.format("%s#%s (%d Votes)", user.getName(), user.getDiscriminator(), pageable.getCount(CurrencyType.CONTEST));
        };
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public enum CurrencyType {
        XP,
        COINS,
        DIAMONDS,
        CONTEST
    }
}
