package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.utils.UserRecord;

public class UserDao {
	private static UserDao instance = null;

	private UserDao() {
	}

	public static UserDao getInstance() {
		if (instance == null) {
			instance = new UserDao();
		}
		return instance;
	}

	public int getUserBalance(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT cw_coins FROM user_table where user_id=" + userId + ";");
			if (rs.next()) {
				return rs.getInt("cw_coins");
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void deductUserBalance(long userId, int coins) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE user_table SET cw_coins = cw_coins - %d where user_id=%d;", coins, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getExtraWordsNumber(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT extra_words FROM user_table WHERE user_id=%d", userId));
			if (rs.next()) {
				return rs.getInt("extra_words");
			}
			return 0;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void updateExtraWordCount(long userId, int count, boolean increment) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			var stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE user_table SET extra_words = extra_words %s %d WHERE user_id=%d;",
					increment ? "+" : "-", count, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void claimCoinsWithExtraWords(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(
					"UPDATE user_table set extra_words=0, cw_coins = cw_coins + 100 WHERE user_id=%d and extra_words >= 25;", userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDailyRewards(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			String todayDate = UtilService.getInstance().getDate();
			stmt.executeUpdate(String.format(
					"INSERT INTO user_table (user_id,coins, cw_coins, last_daily) VALUES (%d, %d, %d, '%s') ON DUPLICATE KEY UPDATE coins = coins + VALUES(coins), cw_coins = cw_coins + VALUES(cw_coins), last_daily = VALUES(last_daily);",
					userId,1000, 100, todayDate));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// public void addCoins(long userId, int coins) {
	// 	Connection conn = ConnectionProvider.getConnection();
	// 	try {
	// 		Statement stmt = conn.createStatement();
	// 		stmt.executeUpdate(String.format(
	// 				"INSERT INTO users (id, coins) VALUES (%d, %d) ON DUPLICATE KEY UPDATE coins = coins + VALUES(coins);",
	// 				userId, coins));
	// 	} catch (SQLException e) {
	// 		e.printStackTrace();
	// 	}
	// }

	// public void updateUserLastDailyDate(long userId) {
	// 	Connection conn = ConnectionProvider.getConnection();
	// 	try {
	// 		Statement stmt = conn.createStatement();
	// 		String todayDate = UtilService.getInstance().getDate();
	// 		stmt.executeUpdate("UPDATE users SET last_daily_crossword='" + todayDate + "' WHERE id=" + userId + ";");
	// 	} catch (SQLException e) {
	// 		e.printStackTrace();
	// 	}
	// }

	public Optional<String> getUserLastDailyDate(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT last_daily FROM user_table WHERE user_id=" + userId + ";");
			if (rs.next()) {
				String lastDate = rs.getString("last_daily");
				return Optional.of(lastDate);
			} else {
				return Optional.empty();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	public boolean addWord(String word) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		var stmt = conn.createStatement();
		var rs = stmt.executeQuery(String.format("Select * from wordlist where words='%s'", word));
		if (rs.next()) {
			return false;
		}
		stmt.executeUpdate(String.format("Insert INTO wordlist (words) values ('%s');", word));
		return true;

	}

	public boolean removeWord(String word) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		var stmt = conn.createStatement();
		var rs = stmt.executeQuery(String.format("Select * from wordlist where words='%s'", word));
		if (rs.next()) {
			stmt.executeUpdate(String.format("Delete from wordlist where words='%s';", word));
			return true;
		}
		return false;
	}

	public List<UserRecord> getTopUsersBasedOnLevel(int limit) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT user_id,level FROM user_table ORDER BY level DESC LIMIT " + limit + ";");
		List<UserRecord> records = new ArrayList<>(limit);
		while (rs.next()) {
			records.add(new UserRecord(rs.getLong("user_id"), rs.getInt("level")-1));
		}
		return records;
	}

}