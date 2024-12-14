package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class StocksDao {

  private static StocksDao stocksDao = null;

  private StocksDao() {
  };

  public static StocksDao getInstance() {
    if (stocksDao == null) {
      stocksDao = new StocksDao();
    }
    return stocksDao;
  }

  public Map<String, Integer> getStocksValue() {
    Map<String, Integer> stocksMap = new HashMap<>();
    try {
      Connection conn = ConnectionProvider.getConnection();
      Statement stmt = conn.createStatement();
      String query = "select company,price from stock_values;";
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        stocksMap.put(rs.getString("company"), rs.getInt("price"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return stocksMap;
  }

  public boolean sellStocks(long userId, String company, int stocksCount, int stockPrice) {
    try {
      Connection conn = ConnectionProvider.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet sRS = stmt.executeQuery("SELECT " + company + " FROM stocks WHERE user_id=" + userId + ";");
      int numberOfStocksOwned = 0;
      if (sRS.next()) {
        numberOfStocksOwned = sRS.getInt(company);
      }
      if (numberOfStocksOwned < stocksCount) {
        return false;
      }
      long costReceieved = (long)stockPrice * (long)stocksCount;
      if (costReceieved < 0) {
        return false;
      }
      stmt.executeUpdate(
          String.format("UPDATE stocks SET %s=%s - %d WHERE user_id=%d;", company, company, stocksCount, userId));
      stmt.executeUpdate(String.format("UPDATE coin_table SET coins = coins + %d where user_id=%d;",
          costReceieved, userId));
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public int[] getUserStocks(long userId, String[] companyArry) {
    int[] returnArray = new int[companyArry.length];
    try {
      Connection conn = ConnectionProvider.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet sRS = stmt.executeQuery("SELECT * FROM stocks WHERE user_id=" + userId + ";");
      if (sRS.next()) {
        for (int i = 0; i < companyArry.length; i++) {
          returnArray[i] = sRS.getInt(companyArry[i]);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return returnArray;
  }

  public void deleteStocksData(long userId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM stocks WHERE user_id=" + userId + ";");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
