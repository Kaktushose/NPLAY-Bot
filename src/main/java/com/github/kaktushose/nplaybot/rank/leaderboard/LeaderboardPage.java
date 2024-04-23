package com.github.kaktushose.nplaybot.rank.leaderboard;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;
import java.util.Optional;

public record LeaderboardPage(List<LeaderboardRow> rows) {
    public String getPage(Guild guild) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i);
            appendRow(builder, i + 1, resolveName(guild, row.userId), row.xp, String.format("<@&%d>", row.roleId));
        }
        return builder.toString();
    }

    private void appendRow(StringBuilder builder, int index, String username, int xp, String role) {
        builder.append(String.format("%d. %s %d XP (%s)\n", index, username, xp, role));
    }

    private String resolveName(Guild guild, long userId) {
        var member = Optional.ofNullable(guild.getMemberById(userId));
        if (member.isPresent()) {
            return member.get().getEffectiveName();
        }
        // retrieve member thus it gets loaded to cache
        guild.retrieveMemberById(userId).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
        return String.format("<@%d>", userId);
    }

    public record LeaderboardRow(int xp, long userId, long roleId) {
    }

}
