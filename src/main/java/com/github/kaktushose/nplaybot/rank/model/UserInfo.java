package com.github.kaktushose.nplaybot.rank.model;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record UserInfo(int currentXp, RankInfo currentRank, Optional<RankInfo> nextRank, int messageCount, int xpGain,
                       int karma) {

    public Map<String, Object> getEmbedValues(User user) {
        var result = new HashMap<String, Object>() {{
            put("user", String.format("<@%d>", user.getIdLong()));
            put("color", currentRank.color());
            put("avatarUrl", user.getEffectiveAvatarUrl());
            put("currentRank", currentRank.name());
            put("currentXp", currentXp);
            put("karma", karma);
            put("xpGain", xpGain);
            put("messageCount", messageCount);
        }};
        nextRank.ifPresent(rank -> {
            result.put("nextRank", rank.name());
            result.put("missingXp", rank.xpBound() - currentXp);
        });
        return result;
    }
}
