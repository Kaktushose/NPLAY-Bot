package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.rank.model.RankInfo;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RankService {

    private final DataSource dataSource;

    public RankService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Optional<RankInfo> getRankInfo(int rankId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT role_id, color, bound
                    FROM ranks
                    WHERE rank_id = ?
                    """
            );
            statement.setLong(1, rankId);

            var result = statement.executeQuery();

            if (result.next()) {
                return Optional.of(new RankInfo(
                        result.getLong("role_id"),
                        result.getString("color"),
                        result.getInt("bound")
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserInfo getUserInfo(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT xp, rank_id, message_count, start_xp
                    FROM users
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, userId);

            var result = statement.executeQuery();
            result.next();

            var currentRank = getRankInfo(result.getInt("rank_id")).orElseThrow();
            var nextRank = getRankInfo(result.getInt("rank_id") + 1);

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

    public void updateValidMessage(User user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE users
                    SET last_valid_message = ?
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, System.currentTimeMillis());
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public XpChangeResult addRandomXp(User user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM add_random_xp(?)");
            statement.setLong(1, user.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return new XpChangeResult(
                    result.getBoolean("rank_changed"),
                    getRankInfo(result.getInt("current_rank")).orElseThrow(),
                    getRankInfo(result.getInt("next_rank")),
                    result.getInt("current_xp")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> getRankRoleIds() {
        try (Connection connection = dataSource.getConnection()) {
            var result = connection.prepareStatement("SELECT role_id FROM ranks").executeQuery();
            var roleIds = new ArrayList<Long>();
            while (result.next()) {
                roleIds.add(result.getLong(1));
            }
            return roleIds;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
