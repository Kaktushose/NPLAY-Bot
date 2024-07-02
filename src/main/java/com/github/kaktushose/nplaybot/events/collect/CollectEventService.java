package com.github.kaktushose.nplaybot.events.collect;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.contest.ContestEventService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CollectEventService {


    private static final Logger log = LoggerFactory.getLogger(ContestEventService.class);
    private final DataSource dataSource;
    private final Guild guild;

    public CollectEventService(DataSource dataSource, Bot bot) {
        this.dataSource = dataSource;
        this.guild = bot.getGuild();
    }

    public boolean isCollectEventActive() {
        log.debug("Querying collect event active flag for guild {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT collect_event_active
                    FROM event_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startCollectEvent(String eventName, String currencyName, String emoji) {
        log.debug("Starting new collect event [{}, {}, {}] for guild {}", eventName, currencyName, emoji, guild);
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("UPDATE users SET collect_points = 0").execute();

            var statement = connection.prepareStatement("""
                    UPDATE event_settings
                    SET collect_event_name = ?,
                    collect_currency_name = ?,
                    collect_currency_emoji = ?,
                    collect_event_active = true
                    WHERE guild_id = ?
                    """
            );

            statement.setString(1, eventName);
            statement.setString(2, currencyName);
            statement.setString(3, emoji);
            statement.setLong(4, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopCollectEvent() {
        log.debug("Stopping collect event for guild {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE event_settings
                    SET collect_event_active = false
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createCollectReward(String name, int threshold, int xp, Role role, String embed) {
        log.debug("Creating new collect reward");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO collect_rewards(name, threshold, xp, role_id, embed) VALUES (?, ?, ?, ?, CAST (? AS jsonb))");
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

    public void updateCollectLootChance(double chance) {
        log.debug("Updating collect loot chance for guild: {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE event_settings
                    SET collect_loot_chance = ?
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

    public int getCollectLootDrop(Message message) {
        log.debug("Querying collect loot drop for message {}", message);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM get_collect_loot_drop(?)");
            statement.setLong(1, message.getGuildIdLong());
            var result = statement.executeQuery();
            result.next();
            var points = result.getInt(1);
            log.debug("Collect loot drop: {} points", points);
            return points;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CollectCurrency getCollectCurrency() {
        log.debug("Querying collect currency emoji");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT collect_currency_emoji, collect_currency_name
                    FROM event_settings
                    WHERE guild_id = ?
                    """);
            statement.setLong(1, guild.getIdLong());
            var result = statement.executeQuery();
            result.next();
            return new CollectCurrency(
                    result.getString("collect_currency_name"),
                    result.getString("collect_currency_emoji")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CollectReward> getCollectRewards() {
        log.debug("Querying collect rewards");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM collect_rewards");
            var result = statement.executeQuery();
            List<CollectReward> rewards = new ArrayList<>();
            while (result.next()) {
                rewards.add(new CollectReward(
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

    public void deleteCollectReward(int rewardId) {
        log.debug("Deleting collect reward with id {}", rewardId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM collect_rewards WHERE reward_id = ?");
            statement.setInt(1, rewardId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int addCollectPoint(UserSnowflake user) {
        log.debug("Adding one collect point to user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("UPDATE users SET collect_points = collect_points + 1 where user_id = ?");
            statement.setLong(1, user.getIdLong());
            statement.execute();
            return getCollectPoints(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCollectPoints(UserSnowflake user) {
        log.debug("Querying collect points for user {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT collect_points FROM users WHERE user_Id = ?");
            statement.setLong(1, user.getIdLong());
            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record CollectCurrency(String name, String emoji) {
    }

    public record CollectReward(int rewardId, String name, int threshold, int xp, long roleId, String embed) {
    }
}
