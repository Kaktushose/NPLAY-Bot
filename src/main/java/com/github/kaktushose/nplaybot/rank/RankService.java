package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.rank.leaderboard.LeaderboardPage;
import com.github.kaktushose.nplaybot.rank.model.RankConfig;
import com.github.kaktushose.nplaybot.rank.model.RankInfo;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class RankService {

    private static final Logger log = LoggerFactory.getLogger(RankService.class);
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
                    SELECT role_id, name, color, bound
                    FROM ranks
                    WHERE rank_id = ?
                    """
            );
            statement.setLong(1, rankId);

            var result = statement.executeQuery();

            if (result.next()) {
                return Optional.of(new RankInfo(
                        result.getLong("role_id"),
                        result.getString("name"),
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
                    SELECT xp, rank_id, message_count, start_xp, karma_points
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
                    result.getInt("xp") - result.getInt("start_xp"),
                    result.getInt("karma_points")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RankConfig getRankConfig(Guild guild) {
        log.debug("Querying rank config for guild: {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT message_cooldown, min_message_length, xp_loot_chance
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();

            return new RankConfig(
                    result.getInt("message_cooldown"),
                    result.getInt("min_message_length"),
                    result.getDouble("xp_loot_chance")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCooldown(Guild guild, int cooldown) {
        log.debug("Updating cooldown for guild: {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET message_cooldown = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setInt(1, cooldown);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMinMessageLength(Guild guild, int length) {
        log.debug("Updating minimum message length for guild: {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET min_message_length = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setInt(1, length);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateXpLootChance(Guild guild, double chance) {
        log.debug("Updating xp loot chance for guild: {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET xp_loot_chance = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setDouble(1, chance);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Long> getValidChannels(Guild guild) {
        log.debug("Querying valid channels for guild {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT valid_channels
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return new HashSet<>(Arrays.asList((Long[]) result.getArray("valid_channels").getArray()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateValidChannels(Guild guild, Set<Long> validChannels) {
        log.debug("Querying valid channels for guild {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET valid_channels = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setArray(1, connection.createArrayOf("BIGINT", validChannels.toArray()));
            statement.setLong(2, guild.getIdLong());

            statement.execute();
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

            if (System.currentTimeMillis() - lastMessage < messageCooldown) {
                log.debug("User still has cooldown ({} < {})", System.currentTimeMillis() - lastMessage, messageCooldown);
                return false;
            }

            if (message.getContentDisplay().length() < minimumLength) {
                log.debug("Message is too short (Length: {}, Required: {}", message.getContentDisplay().length(), minimumLength);
                return false;
            }

            log.debug("Message is valid");
            return true;
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

    public Optional<MessageCreateData> onXpChange(XpChangeResult result, Member member, Guild guild, EmbedCache embedCache) {
        log.debug("Checking for rank up: {}", member);
        updateRankRoles(member, guild, result);

        if (!result.rankChanged()) {
            log.debug("Rank hasn't changed");
            return Optional.empty();
        }
        log.debug("Applying changes. New rank: {}", result.currentRank());

        var embed = result.nextRank().isPresent() ? "rankIncrease" : "rankIncreaseMax";
        return Optional.of(new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(embedCache.getEmbed(embed).injectValues(result.getEmbedValues(member)).toMessageEmbed())
                .build());
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

    public void switchDaily(UserSnowflake user, boolean enabled) {
        log.debug("Setting daily message for user {} to {}", user, enabled);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("UPDATE users SET daily_message = ? where user_id = ?");
            statement.setBoolean(1, enabled);
            statement.setLong(2, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Long, UserInfo> getDailyRankInfos() {
        log.debug("Querying daily rank infos");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT user_id, xp, rank_id, message_count, start_xp, karma_points
                    FROM users
                    WHERE daily_message = true
                    """
            );
            var result = statement.executeQuery();
            var users = new HashMap<Long, UserInfo>();
            while (result.next()) {
                var currentRank = getRankInfo(result.getInt("rank_id")).orElseThrow();
                var nextRank = getRankInfo(result.getInt("rank_id") + 1);

                users.put(result.getLong("user_id"), new UserInfo(
                        result.getInt("xp"),
                        currentRank,
                        nextRank,
                        result.getInt("message_count"),
                        result.getInt("xp") - result.getInt("start_xp"),
                        result.getInt("karma_points")
                ));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getXpLootDrop(Message message) {
        log.debug("Querying xp loot drop for message {}", message);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM get_xp_loot_drop(?)");
            statement.setLong(1, message.getGuildIdLong());

            var result = statement.executeQuery();
            result.next();
            var xp = result.getInt(1);
            log.debug("Xp loot drop: {} xp", xp);
            return xp;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidChannel(MessageChannelUnion channel, Guild guild) {
        log.debug("Checking channel: {}", channel);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_settings.valid_channels
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            var validChannels = Arrays.asList((Long[]) result.getArray("valid_channels").getArray());

            var channelId = channel.getIdLong();
            if (channel.getType().isThread()) {
                channelId = channel.asThreadChannel().getParentChannel().getIdLong();
            }
            var valid = validChannels.contains(channelId);

            if (valid) {
                log.debug("Channel is valid");
            } else {
                log.debug("Invalid message channel");
            }

            return valid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
