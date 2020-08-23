package de.kaktushose.nrtv.discord.util;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard {

    private List<Long> users;

    public Leaderboard() {
        users = new ArrayList<>();
    }

    public void add(long id) {
        users.add(id);
    }

    public List<Long> getPage(int page) {
        page++;
        int from, to;
        from = page * 10 - 10;
        to = page * 10;
        if (users.size() < page * 10 + 1) {
            to = users.size();
        }
        return users.subList(from, to);
    }

    public int getListSize() {
        return (int) Math.ceil(users.size() / 10.0);
    }

}
