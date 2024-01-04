package com.github.kaktushose.nplaybot.rank.model;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record UserInfo(int currentXp, RankInfo currentRank, Optional<RankInfo> nextRank, int messageCount, int xpGain) {

    public Map<String, Object> getEmbedValues(Member member) {
        var result = new HashMap<String, Object>() {{
            put("user", String.format("<@%d>", member.getIdLong()));
            put("color", currentRank.color());
            put("avatarUrl", member.getEffectiveAvatarUrl());
            put("currentRank", String.format("<@&%d>", currentRank.roleId()));
            put("currentXp", currentXp);
            put("xpGain", xpGain);
            put("messageCount", messageCount);
        }};
        nextRank.ifPresent(rank -> {
            result.put("nextRank", String.format("<@&%d>", rank.roleId()));
            result.put("missingXp", rank.xpBound() - currentXp);
        });
        return result;
    }

    public Map<String, Object> getEmbedValues(User user) {
        var result = new HashMap<String, Object>() {{
            put("user", String.format("<@%d>", user.getIdLong()));
            put("color", currentRank.color());
            put("avatarUrl", user.getEffectiveAvatarUrl());
            put("currentRank", currentRank.name());
            put("currentXp", currentXp);
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
