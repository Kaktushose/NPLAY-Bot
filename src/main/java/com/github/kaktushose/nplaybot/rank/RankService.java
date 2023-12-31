package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.rank.leaderboard.LeaderboardPage;
import com.github.kaktushose.nplaybot.rank.model.RankInfo;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;

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

    public void addUser(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO users VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeUser(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?");
            statement.setLong(1, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void indexMembers(Guild guild) {
        guild.loadMembers(member -> addUser(member.getUser()));
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

    public UserInfo getUserInfo(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT xp, rank_id, message_count, start_xp
                    FROM users
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, user.getIdLong());

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

    public void updateValidMessage(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE users
                    SET last_valid_message = ?,
                    message_count = message_count + 1
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

    public XpChangeResult addRandomXp(UserSnowflake user) {
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

    public XpChangeResult addXp(UserSnowflake user, int amount) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM add_xp(?, ?)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, amount);

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

    public XpChangeResult setXp(UserSnowflake user, int value) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM set_xp(?, ?)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, value);

            var result = statement.executeQuery();
            result.next();
            return new XpChangeResult(
                    result.getBoolean("rank_changed"),
                    getRankInfo(result.getInt("current_rank")).orElseThrow(),
                    getRankInfo(result.getInt("next_rank")),
                    value
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRankRoles(Member member, Guild guild, XpChangeResult result) {
        var validRole = guild.getRoleById(result.currentRank().roleId());
        var invalidRoles = getRankRoleIds().stream()
                .map(guild::getRoleById)
                .filter(it -> it != validRole)
                .toList();
        guild.modifyMemberRoles(member, List.of(validRole), invalidRoles).queue();
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

    public List<LeaderboardPage> getLeaderboard() {
        try (Connection connection = dataSource.getConnection()) {
            var result = connection.prepareStatement("""
                    SELECT users.xp, users.user_id, ranks.role_id
                    FROM users JOIN ranks ON ranks.rank_id = users.rank_id
                    ORDER BY xp DESC;
                    """
            ).executeQuery();

            List<LeaderboardPage> pages = new ArrayList<>();
            List<LeaderboardPage.LeaderboardRow> rows = new ArrayList<>();
            int rowCount = 1;
            while (result.next()) {
                if (rowCount == 11) {
                    pages.add(new LeaderboardPage(rows));
                    rows = new ArrayList<>();
                    rowCount = 1;
                }
                rows.add(new LeaderboardPage.LeaderboardRow(
                        result.getInt("xp"),
                        result.getLong("user_id"),
                        result.getLong("role_id")
                ));
                rowCount++;
            }
            return pages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetDailyStatistics() {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("UPDATE users SET start_xp = xp").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void increaseTotalMessageCount() {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("SELECT * FROM increase_total_message_count()").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
