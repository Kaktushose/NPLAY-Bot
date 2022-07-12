package de.kaktushose.levelbot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.collections4.map.LRUMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Pagination {

    private final static Map<Long, String> nameCache = new LRUMap<>(300);
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
        if (index + 1 < pages()) {
            index++;
            loadNextPage();
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

    private void loadNextPage() {
        int from = (index + 1) * pageSize;
        int to = (index + 1) * pageSize + pageSize;

        if (to > leaderboard.size()) {
            to = leaderboard.size();
        }

        leaderboard.subList(from, to).forEach(this::format);
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
        return switch (currencyType) {
            case XP -> String.format("%s (%d XP)", getNameDisplay(pageable.getUserId()), pageable.getCount(CurrencyType.XP));
            case COINS -> String.format("%s (%d Münzen)", getNameDisplay(pageable.getUserId()), pageable.getCount(CurrencyType.COINS));
            case DIAMONDS -> String.format("%s (%d Diamanten)", getNameDisplay(pageable.getUserId()), pageable.getCount(CurrencyType.DIAMONDS));
            case CONTEST -> String.format("%s (%d Votes)", getNameDisplay(pageable.getUserId()), pageable.getCount(CurrencyType.CONTEST));
        };
    }

    private String getNameDisplay(long id) {
        if (nameCache.containsKey(id)) {
            return nameCache.get(id);
        }

        User user = jda.getUserById(id);
        if (user != null) {
            String format = String.format("%s#%s", user.getName(), user.getDiscriminator());
            nameCache.putIfAbsent(id, format);
            return format;
        }

        jda.retrieveUserById(id).queue(it -> nameCache.putIfAbsent(id, String.format("%s#%s", it.getName(), it.getDiscriminator())));

        return "<@%s>".formatted(id);
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
