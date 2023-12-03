package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.rank.model.Rank;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;

import java.sql.Connection;
import java.sql.SQLException;

public class RankService {

    private final Database database;

    public RankService(Database database) {
        this.database = database;
    }

    public UserInfo getUserInfo(long userId) {
        try (Connection connection = database.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT xp, rank, message_count, start_xp, role_id, color, bound
                    FROM users JOIN ranks
                    WHERE user_id = ? AND rank_id = rank
                    """
            );
            statement.setLong(1, userId);

            var result = statement.executeQuery();
            result.next();
            var currentRank = new Rank(
                    result.getLong("role_id"),
                    result.getString("color"),
                    result.getInt("bound") - result.getInt("xp")
            );

            statement = connection.prepareStatement("""
                    SELECT role_id, color, bound
                    FROM ranks
                    WHERE rank_id = ?
                    """
            );
            statement.setLong(1, result.getInt("rank") + 1);
            result = statement.executeQuery();
            Rank nextRank = null;
            if (result.next()) {
                nextRank = new Rank(
                        result.getLong("role_id"),
                        result.getString("color"),
                        result.getInt("bound") - result.getInt("xp")
                );
            }

            return new UserInfo(
                    result.getInt("xp"),
                    currentRank,
                    nextRank,
                    result.getInt("message_count"),
                    result.getInt("xp") - result.getInt("start_xp")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
