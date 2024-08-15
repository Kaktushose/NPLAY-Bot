package com.github.kaktushose.nplaybot.features.rank.model;

import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record XpChangeResult(boolean rankChanged, Optional<RankInfo> previousRank, RankInfo currentRank,
                             Optional<RankInfo> nextRank, int currentXp) {

    public Map<String, Object> getEmbedValues(UserSnowflake user) {
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

    @Override
    public String toString() {
        return "XpChangeResult{" +
               "rankChanged=" + rankChanged +
               ", currentRank=" + currentRank +
               ", nextRank=" + nextRank +
               ", currentXp=" + currentXp +
               '}';
    }
}
