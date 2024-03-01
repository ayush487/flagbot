package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.stocks.Company;
import com.ayushtech.flagbot.stocks.StocksTransaction;
import com.ayushtech.flagbot.stocks.TransactionType;

public class StocksTransactionsDao {

  private static StocksTransactionsDao stocksTransactionsDao = null;

  private StocksTransactionsDao() {
  }

  public static StocksTransactionsDao getInstance() {
    if (stocksTransactionsDao == null) {
      stocksTransactionsDao = new StocksTransactionsDao();
    }
    return stocksTransactionsDao;
  }

  public void addTransactionData(StocksTransaction transaction) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      String query = String.format(
          "INSERT INTO stocks_transactions (user_id, type, timestamp, company, count, price) values (%d, '%s', %d, '%s', %d, %d);",
          transaction.getUserId(),
          transaction.getType(),
          transaction.getTimeStamp(),
          transaction.getCompany(),
          transaction.getCount(),
          transaction.getPrice());
      stmt.executeUpdate(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<StocksTransaction> getTransactions(long userId, int page, int limit) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet transactionsRS = stmt
          .executeQuery(String.format("Select user_id,type, company,count,timestamp,price from stocks_transactions where user_id=%d ORDER BY timestamp desc limit %d offset %d;", userId,limit, page*limit));
      List<StocksTransaction> transactionsList = new ArrayList<>(5);
      while (transactionsRS.next()) {
        transactionsList.add(new StocksTransaction(
            transactionsRS.getLong("user_id"),
            TransactionType.valueOf(transactionsRS.getString("type")),
            Company.valueOf(transactionsRS.getString("company")),
            transactionsRS.getInt("count"),
            transactionsRS.getLong("timestamp"),
            transactionsRS.getInt("price")));
      }
      return transactionsList;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void deleteTransactionData(long user_id) {
    try {
      Connection conn = ConnectionProvider.getConnection();
      Statement stmt = conn.createStatement();
      stmt.executeUpdate("DELETE FROM stocks_transactions WHERE user_id=" + user_id + ";");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
