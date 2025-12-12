package com.api.account.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {

  private static final String DATABASE_PATH =
      System.getenv().getOrDefault("H2_DATABASE_PATH", "~/test");
  private static final String DATABASE_URL = "jdbc:h2:" + DATABASE_PATH;
  private static final String DATABASE_USER = "sa";
  private static final String DATABASE_PASSWORD = "";
  private static final HikariDataSource DATA_SOURCE;

  static {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(DATABASE_URL);
    config.setUsername(DATABASE_USER);
    config.setPassword(DATABASE_PASSWORD);

    // Pool configuration
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(5);
    config.setConnectionTimeout(30000); // 30 seconds
    config.setIdleTimeout(600000); // 10 minutes
    config.setMaxLifetime(1800000); // 30 minutes
    config.setLeakDetectionThreshold(60000); // 60 seconds

    // H2
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    DATA_SOURCE = new HikariDataSource(config);
  }

  public static Connection getConnection() throws SQLException {
    return DATA_SOURCE.getConnection();
  }

  public static void shutdown() {
    if (!DATA_SOURCE.isClosed()) {
      DATA_SOURCE.close();
    }
  }
}
