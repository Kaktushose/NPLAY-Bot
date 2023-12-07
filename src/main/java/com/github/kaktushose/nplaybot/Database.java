package com.github.kaktushose.nplaybot;

import com.github.kaktushose.nplaybot.settings.SettingsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private final HikariDataSource dataSource;
    private final SettingsService settingsService;

    public Database() {
        var config = new HikariConfig();

        System.out.println(System.getenv("POSTGRES_URL"));
        config.setJdbcUrl(System.getenv("POSTGRES_URL"));
        config.setUsername(System.getenv("POSTGRES_USER"));
        config.setPassword(System.getenv("POSTGRES_PASSWORD"));
        config.addDataSourceProperty("databaseName", System.getenv("POSTGRES_DB"));

        dataSource = new HikariDataSource(config);

        settingsService = new SettingsService(dataSource);
    }

    public void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }
}
