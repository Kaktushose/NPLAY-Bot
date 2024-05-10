package com.github.kaktushose.nplaybot;

import com.github.kaktushose.nplaybot.events.collect.CollectEventService;
import com.github.kaktushose.nplaybot.events.contest.ContestEventService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import com.github.kaktushose.nplaybot.rank.RankService;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {

    private final HikariDataSource dataSource;
    private final SettingsService settingsService;
    private final RankService rankService;
    private final ContestEventService contestEventService;
    private final CollectEventService collectEventService;
    private final PermissionsService permissionsService;

    public Database() {
        var config = new HikariConfig();

        config.setJdbcUrl(System.getenv("POSTGRES_URL"));
        config.setUsername(System.getenv("POSTGRES_USER"));
        config.setPassword(System.getenv("POSTGRES_PASSWORD"));
        config.addDataSourceProperty("databaseName", System.getenv("POSTGRES_DB"));

        dataSource = new HikariDataSource(config);

        settingsService = new SettingsService(dataSource);
        rankService = new RankService(dataSource);
        contestEventService = new ContestEventService(dataSource);
        collectEventService = new CollectEventService(dataSource);
        permissionsService = new PermissionsService(dataSource);
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
}
