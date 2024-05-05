package com.github.kaktushose.nplaybot.events.collect;

import com.github.kaktushose.nplaybot.events.contest.ContestEventService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
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
    public CollectEventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isActive(Guild guild) {
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

    public void startCollectEvent(Guild guild, String eventName, String currencyName, String emoji) {
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
            statement.setString(2,currencyName);
            statement.setString(3, emoji);
            statement.setLong(4, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopCollectEvent(Guild guild) {
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

    public void updateCollectLootChance(Guild guild, double chance) {
        log.debug("Updating collect loot chance for guild: {}", guild);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
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

    public record CollectReward(int rewardId, String name) {
    }
    public List<CollectReward> getCollectRewards() {
        log.debug("Querying collect rewards");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT reward_id, name
                    FROM collect_rewards
                    """
            );
            var result = statement.executeQuery();
            List<CollectReward> rewards = new ArrayList<>();
            while (result.next()) {
                rewards.add(new CollectReward(result.getInt("reward_id"), result.getString("name")));
            }
            return rewards;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCollectReward(int rewardId) {
        log.debug("Deleting reward with id {}", rewardId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM collect_rewards WHERE reward_id = ?");
            statement.setInt(1, rewardId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
