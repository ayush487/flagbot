package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.ayushtech.flagbot.services.UtilService;
import com.ayushtech.flagbot.utils.Constants;

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
		try (Connection conn = ConnectionProvider.getConnection()) {
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
		try (Connection conn = ConnectionProvider.getConnection()) {
			var stmt = conn.createStatement();
			stmt.executeUpdate(
					String.format("UPDATE user_table SET cw_coins = cw_coins - %d where user_id=%d;", coins, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getExtraWordsNumber(long userId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			var stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery(String.format("SELECT extra_words FROM user_table WHERE user_id=%d", userId));
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
		try (Connection conn = ConnectionProvider.getConnection()) {
			var stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE user_table SET extra_words = extra_words %s %d WHERE user_id=%d;",
					increment ? "+" : "-", count, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void claimCoinsWithExtraWords(long userId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(
					"UPDATE user_table set extra_words=0, cw_coins = cw_coins + 100 WHERE user_id=%d and extra_words >= 25;",
					userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDailyRewards(long userId, int flagCoin, int wordCoin) {
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

	// public void updateUserLastDailyDate(long userId) {
	// Connection conn = ConnectionProvider.getConnection();
	// try {
	// Statement stmt = conn.createStatement();
	// String todayDate = UtilService.getInstance().getDate();
	// stmt.executeUpdate("UPDATE users SET last_daily_crossword='" + todayDate + "'
	// WHERE id=" + userId + ";");
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }

	public Optional<String> getUserLastDailyDate(long userId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
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

	public boolean addWord(String word) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(String.format("Select * from wordlist where words='%s'", word));
			if (rs.next()) {
				return false;
			}
			stmt.executeUpdate(String.format("Insert INTO wordlist (words) values ('%s');", word));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean removeWord(String word) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			var stmt = conn.createStatement();
			var rs = stmt.executeQuery(String.format("Select * from wordlist where words='%s'", word));
			if (rs.next()) {
				stmt.executeUpdate(String.format("Delete from wordlist where words='%s';", word));
				return true;
			} else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public void updateUsername(long userId, String username) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(
					String.format("UPDATE user_table SET username='%s' WHERE user_id=%d;", username, userId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setUserUpdateVersion(long userId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(
					"INSERT INTO user_table (user_id, latest_update) VALUES (%d, %d) ON DUPLICATE KEY UPDATE latest_update=%d;",
					userId, Constants.UPDATE_VERSION, Constants.UPDATE_VERSION));
		} catch (SQLException e) {
		}
	}

	public int getUserLatestUpdate(long userId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT latest_update FROM user_table where user_id=" + userId + ";");
			if (rs.next())
				return rs.getInt("latest_update");
			return Constants.UPDATE_VERSION;
		} catch (SQLException e) {
			return Constants.UPDATE_VERSION;
		}
	}

	
}