package com.github.kaktushose.nplaybot.rank.model;

import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record UserInfo(long id, int currentXp, RankInfo currentRank, Optional<RankInfo> nextRank, int messageCount,
                       int xpGain, int karma, int lastKarma, long lastValidMessage, int collectPoints) {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static UserInfo fromResultSet(ResultSet result, RankInfo currentRank, Optional<RankInfo> nextRank) throws SQLException {
        return new UserInfo(
                result.getLong("user_id"),
                result.getInt("xp"),
                currentRank,
                nextRank,
                result.getInt("message_count"),
                result.getInt("xp") - result.getInt("start_xp"),
                result.getInt("karma_points"),
                result.getInt("last_karma"),
                result.getLong("last_valid_message"),
                result.getInt("collect_points")
        );
    }

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
            put("bonusXp", getBonusXp(karma));
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

    private String getBonusXp(int karma) {
        if (karma < 0) {
            return "(-1 PLAY-Punkte)";
        }
        if (karma >= 100) {
            return "(+1 PLAY-Punkte)";
        }
        return "";
    }

}
