package com.github.kaktushose.nplaybot.settings;

import com.github.kaktushose.nplaybot.Bot;
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
    private final Guild guild;

    public SettingsService(DataSource dataSource, Bot bot) {
        this.dataSource = dataSource;
        this.guild = bot.getGuild();
    }

    public TextChannel getBotChannel() {
        log.debug("Querying bot channel");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT bot_channel_id
                    FROM bot_settings
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

    public TextChannel getLogChannel() {
        log.debug("Querying log channel");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT log_channel_id
                    FROM bot_settings
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
