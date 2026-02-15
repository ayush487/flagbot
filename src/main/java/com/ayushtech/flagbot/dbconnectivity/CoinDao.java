package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

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
                    "insert into user_table (user_id, coins) values (? , ?) on duplicate key update coins = coins + ?;");
            ps.setLong(1, userId);
            ps.setLong(2, amount);
            ps.setLong(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCwCoins(long userId, int cwCoins) {
        Connection conn = ConnectionProvider.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "insert into user_table (user_id, cw_coins) values (? , ?) on duplicate key update cw_coins = cw_coins + ?;");
            ps.setLong(1, userId);
            ps.setInt(2, cwCoins);
            ps.setInt(3, cwCoins);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCoins(long userId, long coins, int cwCoins) {
        Connection conn = ConnectionProvider.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_table (user_id, coins, cw_coins) values (?, ?, ?) on duplicate key update coins = coins + ?, cw_coins = cw_coins + ?;");

            ps.setLong(1, userId);
            ps.setLong(2, coins);
            ps.setInt(3, cwCoins);
            ps.setLong(4, coins);
            ps.setInt(5, cwCoins);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getBalance(Long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps = conn.prepareStatement("Select coins from user_table where user_id=?");
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
                    "INSERT INTO user_table (user_id, coins) values (%d , %d) on duplicate key update coins = coins + %d;",
                    userId, amount, amount));
            ResultSet rs = stmt.executeQuery(String.format("SELECT coins FROM user_table WHERE user_id=%d;", userId));
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
            PreparedStatement ps = conn.prepareStatement("delete from user_table where user_id=?;");
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long[] getBalanceAndRankWordCoin(long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT u.coins, u.cw_coins, r.rank FROM user_table u JOIN coin_ranking r ON u.user_id=r.user_id WHERE u.user_id=?;");
            ps.setLong(1, userId);
            ResultSet result = ps.executeQuery();
            long[] data = new long[3];
            while (result.next()) {
                data[0] = result.getLong("coins");
                data[1] = result.getLong("cw_coins");
                data[2] = result.getLong("rank");
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        long[] returnArr = { 0, 0, 999999l };
        return returnArr;

    }

    public long resetUserCoins(long userId) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps1 = conn.prepareStatement("Select coins from user_table where user_id=?");
            ps1.setLong(1, userId);
            ResultSet rs = ps1.executeQuery();
            long coin = 0;
            while (rs.next()) {
                coin = rs.getLong("coins");
            }
            PreparedStatement ps = conn.prepareStatement("UPDATE user_table SET coins=0 WHERE user_id=?;");
            ps.setLong(1, userId);
            ps.executeUpdate();
            return coin;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void transferCoinsFromMultipleUsers(long[] userIds, long receiverId, long amount) {
        Connection conn = ConnectionProvider.getConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createCommandToDeductMultipleUsers(userIds, amount));
            stmt.executeUpdate(
                    String.format("UPDATE user_table SET coins = coins + %d WHERE user_id=%d;", amount * userIds.length,
                            receiverId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String createCommandToDeductMultipleUsers(long[] userIds, long amount) {
        StringBuilder sb = new StringBuilder("UPDATE user_table SET coins = coins - ");
        sb.append(amount);
        sb.append(" WHERE user_id in ");
        String usersInsideBrackets = Arrays.stream(userIds).mapToObj(userId -> userId + "")
                .collect(Collectors.joining(",", "(", ")"));
        sb.append(usersInsideBrackets);
        sb.append(";");
        return sb.toString();
    }
}
