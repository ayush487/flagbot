package com.ayush.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CoinDao {
    public void addCoins(Long userId, Long amount) {
        try {
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement ps = conn.prepareStatement("insert into coin_table (user_id, coins) values (? , ?) on duplicate key update coins = coins + ?;");
            ps.setLong(1, userId);
            ps.setLong(2, amount);
            ps.setLong(3, amount);
            ps.executeUpdate();
        } catch (Exception e) {
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
            while(rs.next()) {
                coin = rs.getLong("coins");
            }
            return coin;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }
}
