package com.github.kaktushose.nplaybot.settings;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SettingsService {

    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);
    private final DataSource dataSource;

    public SettingsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getBotToken(long guildId) {
        log.debug("Querying bot token");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT bot_token
                    FROM guild_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guildId);

            var result = statement.executeQuery();
            result.next();
            return result.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TextChannel getBotChannel(Guild guild) {
        log.debug("Querying bot channel");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT bot_channel_id
                    FROM guild_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return guild.getTextChannelById(result.getLong(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
