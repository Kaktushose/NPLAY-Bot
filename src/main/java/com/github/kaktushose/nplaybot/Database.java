package com.github.kaktushose.nplaybot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private final HikariDataSource dataSource;

    public Database() {
        var config = new HikariConfig();

        config.setJdbcUrl(System.getenv("MYSQL_URL"));
        config.setUsername(System.getenv("MYSQL_USER"));
        config.setPassword(System.getenv("MYSQL_PASSWORD"));
        config.addDataSourceProperty("databaseName", System.getenv("MYSQL_DATABASE"));

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
