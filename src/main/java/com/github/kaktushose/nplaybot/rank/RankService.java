package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.rank.leaderboard.LeaderboardPage;
import com.github.kaktushose.nplaybot.rank.model.RankInfo;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RankService {

    private static final Logger log = LoggerFactory.getLogger(RankListener.class);
    private final DataSource dataSource;

    public RankService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createUser(UserSnowflake user) {
        log.debug("Inserting user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO users VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeUser(UserSnowflake user) {
        log.debug("Deleting user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?");
            statement.setLong(1, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void indexMembers(Guild guild) {
        guild.loadMembers(member -> createUser(member.getUser()));
    }

    private Optional<RankInfo> getRankInfo(int rankId) {
        log.debug("Querying rank info for rank: {}", rankId);
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
        log.debug("Querying user info for user: {}", user);
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
        log.debug("Checking message: {}", message);
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
                log.trace("User still has cooldown ({} < {})", System.currentTimeMillis() - lastMessage, messageCooldown);
                return false;
            }

            if (message.getContentDisplay().length() < minimumLength) {
                log.trace("Message is too short (Length: {}, Required: {}", message.getContentDisplay().length(),  minimumLength);
                return false;
            }

            var channelId = message.getChannelIdLong();
            if (message.getChannelType().isThread()) {
                channelId = message.getChannel().asThreadChannel().getParentChannel().getIdLong();
            }
            var valid = validChannels.contains(channelId);

            if (valid) {
                log.debug("Message is valid");
            } else {
                log.trace("Invalid message channel");
            }

            return valid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateValidMessage(UserSnowflake user) {
        log.debug("Setting last_valid_message for user {} to {}", user, System.currentTimeMillis());
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
        log.debug("Adding random xp to user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM add_random_xp(?)");
            statement.setLong(1, user.getIdLong());

            var result = statement.executeQuery();
            result.next();
            log.debug("New xp: {}", result.getInt("current_xp"));
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
        log.debug("Adding {} xp to {}", amount, user);
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
        log.debug("Setting xp of user {} to {}", user, value);
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
        log.debug("Updating roles for {}. Valid role: {}, invalid Roles {}", member, validRole, invalidRoles);
        guild.modifyMemberRoles(member, List.of(validRole), invalidRoles).queue();
    }

    public List<Long> getRankRoleIds() {
        log.debug("Querying all role ids");
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
        log.debug("Querying leaderboard");
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
                log.trace("Adding row {}", rowCount);
                if (rowCount == 11) {
                    log.trace("Page is full, starting new page");
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
            log.debug("Result of leaderboard query: {} pages", pages.size());
            return pages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetDailyStatistics() {
        log.debug("Resetting start_xp for all users");
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("UPDATE users SET start_xp = xp").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void increaseTotalMessageCount() {
        log.debug("Increasing total message count by one");
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("SELECT * FROM increase_total_message_count()").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
