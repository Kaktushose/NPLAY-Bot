package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.rank.model.Rank;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class RankService {

    private final DataSource dataSource;

    public RankService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserInfo getUserInfo(long userId) {
        try (Connection connection = dataSource.getConnection()) {
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

    public boolean isValidMessage(Message message) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_settings.*, users.last_valid_message
                    FROM rank_settings JOIN users ON user_id = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, message.getAuthor().getIdLong());
            statement.setLong(2, message.getGuildIdLong());

            var result = statement.executeQuery();
            result.next();
            var lastMessage = result.getLong("last_valid_message");
            var messageCooldown = result.getInt("message_cooldown");
            var minimumLength = result.getInt("min_message_length");
            var validChannels = Arrays.asList((Long[]) result.getArray("valid_channels").getArray());

            if (System.currentTimeMillis() - lastMessage < messageCooldown) {
                return false;
            }

            if (message.getContentDisplay().length() < minimumLength) {
                return false;
            }

            var channelId = message.getChannelIdLong();
            if (message.getChannelType().isThread()) {
                channelId = message.getChannel().asThreadChannel().getParentChannel().getIdLong();
            }
            return validChannels.contains(channelId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRandomXp() {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM get_random_xp()
                    """
            );

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
