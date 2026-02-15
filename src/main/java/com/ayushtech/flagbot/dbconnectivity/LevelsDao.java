package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ayushtech.flagbot.crossword.Level;
import com.ayushtech.flagbot.utils.LevelData;

public class LevelsDao {

	private static LevelsDao instance = null;

	private LevelsDao() {
	}

	public static LevelsDao getInstance() {
		if (instance == null) {
			instance = new LevelsDao();
		}
		return instance;
	}

	public Level getUserCurrentLevel(long userId) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(String.format(
				"INSERT INTO user_table (user_id, level) SELECT %d, 1 WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE user_id = %d);",
				userId, userId));
		ResultSet rs = stmt.executeQuery(
				"SELECT * FROM levels JOIN user_table ON levels.level=user_table.level WHERE user_table.user_id=" + userId + ";");
		if (rs.next()) {
			return new Level(rs.getInt("level"), rs.getString("main_word"), rs.getString("words"),
					rs.getString("level_data"));
		}
		return null;
	}

	public void promoteUserLevel(long userId, int level) throws SQLException {
		Connection conn = ConnectionProvider.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(String.format("UPDATE user_table SET level=%d WHERE user_id=%d;", level + 1, userId));
	}

	public Set<String> getAllWords() {
		Connection connection = ConnectionProvider.getConnection();
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT words FROM wordlist;");
			Set<String> words = new HashSet<>();
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				words.add(rs.getString("words"));
			}
			return words;
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	// public Optional<Level> getDailyLevel(long userId, String todayDate) throws SQLException {
	// 	Connection conn = ConnectionProvider.getConnection();
	// 	Statement stmt = conn.createStatement();
	// 	ResultSet temprs = stmt.executeQuery("SELECT last_daily_crossword FROM users WHERE id=" + userId + ";");
	// 	boolean hasUserPlayedToday = false;
	// 	if (temprs.next()) {
	// 		hasUserPlayedToday = todayDate.equals(temprs.getString("last_daily_crossword"));
	// 	}
	// 	if (hasUserPlayedToday) {
	// 		return Optional.empty();
	// 	}
	// 	ResultSet rs = stmt
	// 			.executeQuery("SELECT main_word, words, level_data FROM dailylevels WHERE leveldate='" + todayDate + "';");
	// 	if (rs.next()) {
	// 		var level = new Level(0, rs.getString("main_word"), rs.getString("words"), rs.getString("level_data"));
	// 		return Optional.of(level);
	// 	}
	// 	return Optional.empty();
	// }

	// public Optional<DailyCrosswordData> getDailyData(long userId, String date) {
	// 	String query = String.format(
	// 			"SELECT used_hint,enterred_words,extra_words,level_solved,sun FROM daily_cw_cont_data where id=%d and leveldate='%s';",
	// 			userId, date);
	// 	Connection conn = ConnectionProvider.getConnection();
	// 	try {
	// 		Statement stmt = conn.createStatement();
	// 		ResultSet rs = stmt.executeQuery(query);
	// 		if (rs.next()) {
	// 			DailyCrosswordData data = new DailyCrosswordData(userId, date, rs.getString("level_solved"),
	// 					rs.getString("enterred_words"), rs.getString("extra_words"), rs.getBoolean("used_hint"), rs.getInt("sun"));
	// 			return Optional.of(data);
	// 		} else {
	// 			return Optional.empty();
	// 		}
	// 	} catch (SQLException e) {
	// 		e.printStackTrace();
	// 		return Optional.empty();
	// 	}
	// }

	// public void saveDailyLevelData(DailyCrosswordData data) {
	// 	String query = String.format(
	// 			"INSERT INTO daily_cw_cont_data (id, used_hint, enterred_words, extra_words, level_solved, leveldate, sun) VALUES (%d, %b, '%s', '%s', '%s', '%s', %d);",
	// 			data.userId(), data.usedHint(), data.enterredWords(), data.extraWords(), data.unsolvedGrid(), data.date(),
	// 			data.sun());
	// 	Connection conn = ConnectionProvider.getConnection();
	// 	try {
	// 		var stmt = conn.createStatement();
	// 		stmt.executeUpdate("DELETE FROM daily_cw_cont_data WHERE id=" + data.userId() + ";");
	// 		stmt.executeUpdate(query);
	// 	} catch (SQLException e) {
	// 		e.printStackTrace();
	// 	}
	// }

	public void addLevels(List<LevelData> levels) {
		Connection conn = ConnectionProvider.getConnection();
		String query = "INSERT INTO levels (level, main_word, words, level_data) VALUES (?, ?, ?, ?)";
		try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
			for (LevelData level : levels) {
				preparedStatement.setInt(1, level.levelNumber());
				preparedStatement.setString(2, level.mainWord());
				preparedStatement.setString(3, level.words());
				preparedStatement.setString(4, level.levelData());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}