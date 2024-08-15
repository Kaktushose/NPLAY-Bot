package com.github.kaktushose.nplaybot.features.starboard;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class StarboardService {

    private static final Logger log = LoggerFactory.getLogger(StarboardService.class);
    private final DataSource dataSource;
    private final Guild guild;

    public StarboardService(DataSource dataSource, Bot bot) {
        this.dataSource = dataSource;
        this.guild = bot.getGuild();
    }

    public boolean entryExists(long messageId) {
        log.debug("Checking if starboard entry {} exists", messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM starboard_entries WHERE message_id = ?)");
            statement.setLong(1, messageId);
            var result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createEntry(long messageId) {
        log.info("Creating starboard entry for message  {}", messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO starboard_entries VALUES(?)");
            statement.setLong(1, messageId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRewarded(long messageId) {
        log.debug("Checking if entry {} already got rewarded", messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT is_rewarded
                    FROM starboard_entries
                    WHERE message_id = ?
                    """
            );
            statement.setLong(1, messageId);
            var result = statement.executeQuery();
            result.next();
            return result.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRewarded(long messageId) {
        log.info("Marking entry {} as rewarded", messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE starboard_entries
                    SET is_rewarded = TRUE
                    WHERE message_id = ?
                    """
            );
            statement.setLong(1, messageId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getPostId(long messageId) {
        log.debug("Querying starboard post {}", messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT post_id
                    FROM starboard_entries
                    WHERE message_id = ?
                    """
            );
            statement.setLong(1, messageId);
            var result = statement.executeQuery();
            result.next();
            return result.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPostId(long messageId, long postId) {
        log.info("Setting starboard post id to {} for entry {}", postId, messageId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE starboard_entries
                    SET post_id = ?
                    WHERE message_id = ?
                    """
            );
            statement.setLong(1, postId);
            statement.setLong(2, messageId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getThreshold() {
        log.debug("Querying starboard threshold");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT threshold
                    FROM starboard_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());
            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getKarmaReward() {
        log.debug("Querying starboard karma reward");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT karma_reward
                    FROM starboard_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());
            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getStarboardChannelId() {
        log.debug("Querying starboard channel id");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT channel_id
                    FROM starboard_settings
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

    public boolean isPosted(long messageId) {
        return getPostId(messageId) > 0;
    }
}
