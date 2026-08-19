package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.utils.LRUCache;

public class CoinDao {

    private static CoinDao coinDao = null;
    private LRUCache<Long, Long> coinCache;

    private CoinDao() {
        this.coinCache = new LRUCache<>(1000);
    }

    public static synchronized CoinDao getInstance() {
        if (coinDao == null) {
            coinDao = new CoinDao();
        }
        return coinDao;
    }

    public void addCoins(Long userId, Long amount) {
        long userBalance = getBalance(userId);
        coinCache.put(userId, userBalance+amount);
        try (Connection conn = ConnectionProvider.getConnection()) {
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
        try (Connection conn = ConnectionProvider.getConnection()) {
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
        long userBalance = getBalance(userId);
        coinCache.put(userId, userBalance+coins);
        try (Connection conn = ConnectionProvider.getConnection()) {
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

    public void addDailyRewards(long userId, int flagCoin, int wordCoin) {
        long userBalance = getBalance(userId);
        coinCache.put(userId, userBalance+flagCoin);
        try (Connection conn = ConnectionProvider.getConnection()) {
            Statement stmt = conn.createStatement();
            String todayDate = UtilService.getInstance().getDate();
            stmt.executeUpdate(String.format(
                    "INSERT INTO user_table (user_id,coins, cw_coins, last_daily) VALUES (%d, %d, %d, '%s') ON DUPLICATE KEY UPDATE coins = coins + VALUES(coins), cw_coins = cw_coins + VALUES(cw_coins), last_daily = VALUES(last_daily);",
                    userId, flagCoin, wordCoin, todayDate));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getBalance(Long userId) {
        if (coinCache.containsKey(userId))
            return coinCache.get(userId);
        try (Connection conn = ConnectionProvider.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("Select coins from user_table where user_id=?");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            long coin = 0;
            while (rs.next()) {
                coin = rs.getLong("coins");
            }
            coinCache.put(userId, coin);
            return coin;
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public long addCoinsAndGetBalance(long userId, long amount) {
        long balance = getBalance(userId);
        coinCache.put(userId, balance + amount);
        CompletableFuture.runAsync(() -> {
            try (Connection conn = ConnectionProvider.getConnection()) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(String.format(
                        "INSERT INTO user_table (user_id, coins) values (%d , %d) on duplicate key update coins = coins + %d;",
                        userId, amount, amount));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return balance + amount;
    }

    public void deleteData(Long userId) {
        coinCache.remove(userId);
        try (Connection conn = ConnectionProvider.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("delete from user_table where user_id=?;");
            ps.setLong(1, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long[] getBalanceAndRankWordCoin(long userId) {
        try (Connection conn = ConnectionProvider.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT u.coins, u.cw_coins, r.rank FROM user_table u JOIN coin_ranking r ON u.user_id=r.user_id WHERE u.user_id=?;");
            ps.setLong(1, userId);
            ResultSet result = ps.executeQuery();
            long[] data = new long[3];
            while (result.next()) {
                data[0] = result.getLong("coins");
                data[1] = result.getLong("cw_coins");
                data[2] = result.getLong("rank");
            }
            coinCache.put(userId, data[0]);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        long[] returnArr = { 0, 0, 999999l };
        return returnArr;

    }

    public long resetUserCoins(long userId) {
        coinCache.put(userId, 0l);
        try (Connection conn = ConnectionProvider.getConnection()) {
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
        long receiverInitialBalance = getBalance(receiverId);
        coinCache.put(receiverId, receiverInitialBalance + (amount * userIds.length));
        for (long uId : userIds) {
            if (coinCache.containsKey(uId)) {
                long initialBalance = coinCache.get(uId);
                coinCache.put(uId, initialBalance - amount);
            }
        }
        try (Connection conn = ConnectionProvider.getConnection()) {
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
