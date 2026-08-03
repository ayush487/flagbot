package com.ayushtech.flagbot.dbconnectivity;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionProvider {

    // This is the actual connection pool
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        // Core connection settings
        config.setJdbcUrl(DBInfo.url);
        config.setUsername(DBInfo.user);
        config.setPassword(DBInfo.password);

        // Pool sizing and rules
        config.setMaximumPoolSize(5); // Max connections open at once
        config.setMinimumIdle(2); // Keep at least 2 ready during low traffic
        config.setIdleTimeout(600000); // 10 minutes (in milliseconds)
        config.setMaxLifetime(1800000);// 30 minutes (in milliseconds)

        // MySQL specific performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Build the pool
        dataSource = new HikariDataSource(config);
    }

    // A helper method for your application to call
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}