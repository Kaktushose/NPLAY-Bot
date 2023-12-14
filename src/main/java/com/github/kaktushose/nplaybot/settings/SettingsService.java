package com.github.kaktushose.nplaybot.settings;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SettingsService {

    private final DataSource dataSource;

    public SettingsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getBotToken(long guildId) {
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
