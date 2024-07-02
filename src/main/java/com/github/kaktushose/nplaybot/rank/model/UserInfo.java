package com.github.kaktushose.nplaybot.rank.model;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record UserInfo(int currentXp, RankInfo currentRank, Optional<RankInfo> nextRank, int messageCount, int xpGain,
                       int karma, int lastKarma) {

    public Map<String, Object> getEmbedValues(User user, boolean isDM) {
        var result = new HashMap<String, Object>() {{
            put("user", String.format("<@%d>", user.getIdLong()));
            put("color", currentRank.color());
            put("avatarUrl", user.getEffectiveAvatarUrl());
            if (isDM) {
                put("currentRank", currentRank.name());
            } else {
                put("currentRank", String.format("<@&%d>", currentRank.roleId()));
            }
            put("currentXp", currentXp);
            put("karma", karma);
            put("xpGain", xpGain);
            put("messageCount", messageCount);
        }};
        nextRank.ifPresent(rank -> {
            if (isDM) {
                result.put("nextRank", rank.name());
            } else {
                result.put("nextRank", String.format("<@&%d>", rank.roleId()));
            }
            result.put("missingXp", rank.xpBound() - currentXp);
        });
        return result;
    }
}
