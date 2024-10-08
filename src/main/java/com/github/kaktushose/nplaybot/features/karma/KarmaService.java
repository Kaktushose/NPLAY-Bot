package com.github.kaktushose.nplaybot.features.karma;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KarmaService {

    private static final Logger log = LoggerFactory.getLogger(KarmaService.class);
    private final DataSource dataSource;
    private final Guild guild;

    public KarmaService(DataSource dataSource, Bot bot) {
        this.dataSource = dataSource;
        this.guild = bot.getGuild();
    }

    public void setKarma(UserSnowflake user, int karma) {
        log.info("Setting karma of {} to {}", user, karma);
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE users
                    SET karma_points = ?
                    WHERE user_id = ?
                    """);
            statement.setInt(1, karma);
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addKarma(UserSnowflake user, int karma) {
        log.info("Increasing karma points of {} by {}", user, karma);
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE users
                    SET karma_points = karma_points + ?
                    WHERE user_id = ?
                    """);
            statement.setInt(1, karma);
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetTokens() {
        log.debug("Resetting all karma tokens to default value");
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("UPDATE users SET karma_tokens = (SELECT default_tokens FROM karma_settings WHERE guild_id = ?)");
            statement.setLong(1, guild.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void decreaseTokens(UserSnowflake user, int decrease) {
        log.info("Decreasing karma tokens of {} by {}", user, decrease);
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE users
                    SET karma_tokens = karma_tokens - ?
                    WHERE user_id = ?
                    """);
            statement.setInt(1, decrease);
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int onKarmaVoteAdd(UserSnowflake author, UserSnowflake target, boolean decreaseTokens) {
        log.info("Performing karma vote of {} for {}", author, target);
        try (var connection = dataSource.getConnection()) {
            if (getUserTokens(author) > 0) {
                addKarma(target, 1);

                if (decreaseTokens) {
                    decreaseTokens(author, 1);
                }
            }

            var statement = connection.prepareStatement("SELECT karma_points FROM users WHERE user_id = ?");
            statement.setLong(1, target.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int onKarmaVoteRemove(UserSnowflake author, UserSnowflake target, boolean decreaseTokens) {
        log.info("Removing karma vote of {} for {}", author, target);
        try (var connection = dataSource.getConnection()) {
            if (getUserTokens(author) > 0) {
                addKarma(target, -1);

                if (decreaseTokens) {
                    decreaseTokens(author, 1);
                }
            }

            var statement = connection.prepareStatement("SELECT karma_points FROM users WHERE user_id = ?");
            statement.setLong(1, target.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getUserTokens(UserSnowflake user) {
        log.debug("Querying karma tokens for user {}", user);
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT karma_tokens
                    FROM users
                    WHERE user_id = ?
                    """);

            statement.setLong(1, user.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDefaultTokens() {
        log.debug("Querying default karma tokens");
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT default_tokens
                    FROM karma_settings
                    WHERE guild_id = ?
                    """);

            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDefaultTokens(int value) {
        log.info("Setting default karma tokens, new value {}", value);
        try (var connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE karma_settings
                    SET default_tokens = ?
                    WHERE guild_id = ?
                    """);

            statement.setInt(1, value);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UnicodeEmoji> getValidUpvoteEmojis() {
        log.debug("Querying valid karma emojis");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT valid_emojis_upvote
                    FROM karma_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            var emojis = Arrays.asList((String[]) result.getArray(1).getArray());
            return emojis.stream().map(Emoji::fromUnicode).toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UnicodeEmoji> getValidDownvoteEmojis() {
        log.debug("Querying valid karma emojis");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT valid_emojis_downvote
                    FROM karma_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            var emojis = Arrays.asList((String[]) result.getArray(1).getArray());
            return emojis.stream().map(Emoji::fromUnicode).toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createKarmaReward(String name, int threshold, int xp, Role role, String embed) {
        log.info("Creating new karma reward");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO karma_rewards(name, threshold, xp, role_id, embed) VALUES (?, ?, ?, ?, CAST (? AS jsonb))");
            statement.setString(1, name);
            statement.setInt(2, threshold);
            statement.setInt(3, xp);
            statement.setLong(4, role == null ? -1 : role.getIdLong());
            statement.setObject(5, embed);

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<KarmaReward> getKarmaRewards() {
        log.debug("Querying karma rewards");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM karma_rewards");
            var result = statement.executeQuery();
            List<KarmaReward> rewards = new ArrayList<>();
            while (result.next()) {
                rewards.add(new KarmaReward(
                                result.getInt("reward_id"),
                                result.getString("name"),
                                result.getInt("threshold"),
                                result.getInt("xp"),
                                result.getLong("role_id"),
                                result.getString("embed")
                        )
                );
            }
            return rewards;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteKarmaReward(int rewardId) {
        log.info("Deleting karma reward with id {}", rewardId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM karma_rewards WHERE reward_id = ?");
            statement.setInt(1, rewardId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record KarmaReward(int rewardId, String name, int threshold, int xp, long roleId, String embed) {
        @Override
        public String toString() {
            return "KarmaReward{" +
                   "rewardId=" + rewardId +
                   ", name='" + name + '\'' +
                   ", threshold=" + threshold +
                   ", xp=" + xp +
                   ", roleId=" + roleId +
                   ", embed='" + embed + '\'' +
                   '}';
        }
    }
}
