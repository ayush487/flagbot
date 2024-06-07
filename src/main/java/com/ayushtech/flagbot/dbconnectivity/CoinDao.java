package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CoinDao {

    private static CoinDao coinDao = null;

    private CoinDao() {
    }

    public static synchronized CoinDao getInstance() {
        if (coinDao == null) {
            coinDao = new CoinDao();
        }
        return coinDao;
    }

    public void addCoins(Long userId, Long amount) {
        Connection conn = ConnectionProvider.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "insert into coin_table (user_id, coins) values (? , ?) on duplicate key update coins = coins + ?;");
            ps.setLong(1, userId);
            ps.setLong(2, amount);
            ps.setLong(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getBalance(Long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps = conn.prepareStatement("Select coins from coin_table where user_id=?");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            long coin = 0;
            while (rs.next()) {
                coin = rs.getLong("coins");
            }
            return coin;
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public long addCoinsAndGetBalance(long userId, long amount) {
        Connection conn = ConnectionProvider.getConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.format(
                    "INSERT INTO coin_table (user_id, coins) values (%d , %d) on duplicate key update coins = coins + %d;",
                    userId, amount, amount));
            ResultSet rs = stmt.executeQuery(String.format("SELECT coins FROM coin_table WHERE user_id=%d;", userId));
            if (rs.next()) {
                return rs.getLong("coins");
            }
            return 0l;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public void deleteData(Long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps = conn.prepareStatement("delete from coin_table where user_id=?;");
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long[] getBalanceAndRank(long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps = conn.prepareStatement("select coins, ranking from coin_ranking where user_id=?");
            ps.setLong(1, userId);
            ResultSet result = ps.executeQuery();
            long[] balance_rank = new long[2];
            while (result.next()) {
                balance_rank[0] = result.getLong("coins");
                balance_rank[1] = result.getLong("ranking");
            }
            return balance_rank;
        } catch (Exception e) {
            e.printStackTrace();
        }
        long[] returnArr = { 0, 9999l };
        return returnArr;

    }

    public long resetUserCoins(long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps1 = conn.prepareStatement("Select coins from coin_table where user_id=?");
            ps1.setLong(1, userId);
            ResultSet rs = ps1.executeQuery();
            long coin = 0;
            while (rs.next()) {
                coin = rs.getLong("coins");
            }
            PreparedStatement ps = conn.prepareStatement("UPDATE coin_table SET coins=0 WHERE user_id=?;");
            ps.setLong(1, userId);
            ps.executeUpdate();
            return coin;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
