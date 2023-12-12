package com.github.kaktushose.nplaybot.rank.model;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record XpChangeResult(boolean rankChanged, RankInfo currentRank, Optional<RankInfo> nextRank, int currentXp) {

    public Map<String, Object> getEmbedValues(User user) {
        var result = new HashMap<String, Object>() {{
            put("user", String.format("<@%d>", user.getIdLong()));
            put("color", currentRank.color());
            put("currentRank", String.format("<@&%d>", currentRank.roleId()));
        }};
        nextRank.ifPresent(rank -> {
            result.put("nextRank", String.format("<@&%d>", rank.roleId()));
            result.put("xp", rank.xpBound() - currentXp);
        });
        return result;
    }
}
