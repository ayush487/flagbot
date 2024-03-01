package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

  public boolean buyStocks(long userId, String company, int count, int price) {
    try {
      Connection conn = ConnectionProvider.getConnection();
      PreparedStatement coinPS = conn.prepareStatement("Select coins from coin_table where user_id=?");
      coinPS.setLong(1, userId);
      ResultSet coinRS = coinPS.executeQuery();
      long coin = 0;
      while (coinRS.next()) {
        coin = coinRS.getLong("coins");
      }
      long cost = (long)count * price;
      if(cost < 0) return false;
      if (coin < cost) {
        return false;
      }
      Statement stmt = conn.createStatement();
      stmt.executeUpdate("UPDATE coin_table SET coins = coins - " + cost + " where user_id=" + userId + ";");
      String stocksAddQuery = String.format(
          "INSERT INTO stocks(user_id, %s) VALUES (%d , %d) on duplicate key UPDATE %s = %s + %d;", company, userId,
          count, company, company, count);
      stmt.executeUpdate(stocksAddQuery);
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
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

  public void setStockValue(Map<String, Integer> stockMap) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      String query = getQueryToUpdateAllStocks(stockMap);
      stmt.executeUpdate(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }
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

  private String getQueryToUpdateAllStocks(Map<String, Integer> map) {
    String query = map.keySet().stream()
        .map(c -> " when company='" + c + "' then " + map.get(c))
        .collect(Collectors.joining(" ", "update stock_values set price= case  ", " end;"));
    return query;
  }

}
