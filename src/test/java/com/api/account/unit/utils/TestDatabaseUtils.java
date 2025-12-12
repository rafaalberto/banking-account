package com.api.account.unit.utils;

import com.api.account.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabaseUtils {
  public static void deleteAllAccounts() throws SQLException {
    try (Connection conn = ConnectionFactory.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM accounts");
    }
  }
}
