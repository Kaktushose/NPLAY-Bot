package com.github.kaktushose.nplaybot.events.contest;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContestEventService {

    private static final Logger log = LoggerFactory.getLogger(ContestEventService.class);
    private final DataSource dataSource;
    public ContestEventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long getContestEventChannel(Guild guild) {
        log.debug("Querying contest event channel");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT contest_channel_id
                    FROM event_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getVoteEmoji(Guild guild) {
        log.debug("Querying vote emoji");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT contest_vote_emoji
                    FROM event_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startContestEvent(TextChannel channel, String emoji) {
        log.debug("Starting new contest event in {} with vote emoji {}", channel, emoji);
        try (Connection connection = dataSource.getConnection()) {
            log.debug("Truncating old votes");
            connection.prepareStatement("TRUNCATE TABLE contest_entries;").execute();

            var statement = connection.prepareStatement("""
                    UPDATE event_settings
                    SET contest_channel_id = ?,
                    contest_vote_emoji = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, channel.getIdLong());
            statement.setString(2, emoji);
            statement.setLong(3, channel.getGuild().getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ContestLeaderboardRow> stopContestEvent(Guild guild) {
        log.debug("Stopping current contest event");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE event_settings
                    SET contest_channel_id = -1,
                    contest_vote_emoji = ''
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());
            statement.execute();

            log.debug("Querying top 10 entries");
            statement = connection.prepareStatement("""
                    SELECT user_id, votes
                    FROM contest_entries
                    ORDER BY votes DESC LIMIT 10
                    """
            );
            List<ContestLeaderboardRow> users = new ArrayList<>();
            var result = statement.executeQuery();
            while (result.next()) {
                users.add(new ContestLeaderboardRow(result.getInt("votes"), result.getLong("user_id")));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean createContestEntry(Message message) {
        log.debug("Inserting contest entry: {}", message);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO contest_entries VALUES(?, 0, ?) ON CONFLICT DO NOTHING");
            statement.setLong(1, message.getAuthor().getIdLong());
            statement.setLong(2, message.getIdLong());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteContestEntry(long messageId) {
        log.debug("Deleting contest entry: {}", messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM contest_entries WHERE message_id = ?");
            statement.setLong(1, messageId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Increases the vote count for a contest entry.
     *
     * @param messageId the message id representing the contest entry
     * @param userId    the id of the user that voted for the entry
     */
    public void increaseVoteCount(long messageId, long userId) {
        log.debug("Increasing total votes for {} by one", messageId);
        try (Connection connection = dataSource.getConnection()) {
            // this AND clause prevents a self vote of the message author
            var statement = connection.prepareStatement("UPDATE contest_entries SET votes = votes + 1 where message_id = ? AND user_id != ?");
            statement.setLong(1, messageId);
            statement.setLong(2, userId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decreases the vote count for a contest entry.
     *
     * @param messageId the message id representing the contest entry
     * @param userId    the id of the user that removed his vote for the entry
     */
    public void decreaseVoteCount(long messageId, long userId) {
        log.debug("Decreasing total votes for {} by one", messageId);
        try (Connection connection = dataSource.getConnection()) {
            // this AND clause prevents a self vote of the message author
            var statement = connection.prepareStatement("UPDATE contest_entries SET votes = votes - 1 where message_id = ?  AND user_id != ?");
            statement.setLong(1, messageId);
            statement.setLong(2, userId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record ContestLeaderboardRow(int votes, long userId) {
    }

}
