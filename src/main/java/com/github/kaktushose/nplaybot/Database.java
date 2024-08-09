package com.github.kaktushose.nplaybot;

import com.github.kaktushose.nplaybot.events.collect.CollectEventService;
import com.github.kaktushose.nplaybot.events.contest.ContestEventService;
import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.karma.KarmaService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import com.github.kaktushose.nplaybot.rank.RankService;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import com.github.kaktushose.nplaybot.starboard.StarboardService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private final HikariDataSource dataSource;
    private final SettingsService settingsService;
    private final RankService rankService;
    private final ContestEventService contestEventService;
    private final CollectEventService collectEventService;
    private final PermissionsService permissionsService;
    private final KarmaService karmaService;
    private final StarboardService starboardService;
    private final ItemService itemService;

    public Database(Bot bot) {
        var config = new HikariConfig();

        config.setJdbcUrl(System.getenv("POSTGRES_URL"));
        config.setUsername(System.getenv("POSTGRES_USER"));
        config.setPassword(System.getenv("POSTGRES_PASSWORD"));
        config.addDataSourceProperty("databaseName", System.getenv("POSTGRES_DB"));

        dataSource = new HikariDataSource(config);

        initialSetup(bot.getGuild().getIdLong());

        settingsService = new SettingsService(dataSource, bot);
        itemService = new ItemService(dataSource, bot, settingsService);
        rankService = new RankService(dataSource, itemService, bot);
        contestEventService = new ContestEventService(dataSource, bot);
        collectEventService = new CollectEventService(dataSource, bot);
        permissionsService = new PermissionsService(dataSource, bot);
        karmaService = new KarmaService(dataSource, rankService, itemService, bot);
        starboardService = new StarboardService(dataSource, bot);
    }

    private void initialSetup(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            // try to query bot_settings for current guild
            var statement = connection.prepareStatement("SELECT * FROM bot_settings WHERE guild_id = ?");
            statement.setLong(1, guildId);
            var result = statement.executeQuery();

            // if present, guild is already setup
            if (result.next()) {
                return;
            }

            //initialize all settings table with default values
            statement = connection.prepareStatement("INSERT INTO bot_settings VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, guildId);
            statement.execute();

            statement = connection.prepareStatement("INSERT INTO rank_settings VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, guildId);
            statement.execute();

            statement = connection.prepareStatement("INSERT INTO event_settings VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, guildId);
            statement.execute();

            statement = connection.prepareStatement("INSERT INTO karma_settings VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, guildId);
            statement.execute();

            statement = connection.prepareStatement("INSERT INTO starboard_settings VALUES(?) ON CONFLICT DO NOTHING");
            statement.setLong(1, guildId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeDataSource() {
        dataSource.close();
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public RankService getRankService() {
        return rankService;
    }

    public ContestEventService getContestEventService() {
        return contestEventService;
    }

    public CollectEventService getCollectEventService() {
        return collectEventService;
    }

    public PermissionsService getPermissionsService() {
        return permissionsService;
    }

    public KarmaService getKarmaService() {
        return karmaService;
    }

    public StarboardService getStarboardService() {
        return starboardService;
    }

    public ItemService getItemService() {
        return itemService;
    }
}
