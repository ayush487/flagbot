package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.utils.LbEntry;

public class LeaderboardDao {

	// private String[] spaceArray = {
	// "",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " ",
	// " "
	// };

	// public String getPlayers(JDA jda, int players) {
	// try {
	// Connection conn = ConnectionProvider.getConnection();
	// PreparedStatement ps = conn.prepareStatement("select * from user_table order
	// by coins desc limit ?;");
	// ps.setInt(1, players);
	// ResultSet rs = ps.executeQuery();
	// StringBuffer sb = new StringBuffer();
	// int index = 1;
	// long start = System.currentTimeMillis();
	// while (rs.next()) {
	// long userId = rs.getLong("user_id");
	// long amount = rs.getLong("coins");

	// String userTagName = jda.retrieveUserById(userId)
	// .map(user -> user.getName())
	// .complete();
	// int spaces = (18 - userTagName.length()) > 1 ? (18 - userTagName.length()) :
	// 1;
	// sb.append("\n" + index + ". " + userTagName + spaceArray[spaces] + amount + "
	// Coins");
	// index++;
	// }
	// long end = System.currentTimeMillis();
	// System.out.println(end - start);
	// return sb.toString();
	// } catch (SQLException exception) {
	// exception.printStackTrace();
	// return null;
	// }
	// }

	public List<LbEntry> getPlayersBasedOnCoins(int size, long offset) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(
					"SELECT user_id, coins from user_table order by coins desc limit %d offset %d;", size, offset));
			ArrayList<LbEntry> entries = new ArrayList<>();
			while (rs.next()) {
				var entry = new LbEntry(++offset, rs.getLong("user_id"), rs.getLong("coins"));
				entries.add(entry);
			}
			return entries;
		} catch (SQLException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<LbEntry> getPlayersBasedOnLevels(int size, long offset) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(
					"SELECT user_id, level from user_table order by level desc limit %d offset %d;", size, offset));
			ArrayList<LbEntry> entries = new ArrayList<>();
			while (rs.next()) {
				var entry = new LbEntry(++offset, rs.getLong("user_id"), rs.getInt("level"));
				entries.add(entry);
			}
			return entries;
		} catch (SQLException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public long getPlayerCoinRank(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("select * from coin_ranking where user_id=%d;", userId));
			if (rs.next()) {
				return rs.getLong("rank");
			}
			return 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return 1;
		}
	}

	public long getPlayerLevelRank(long userId) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("select * from level_ranking where user_id=%d;", userId));
			if (rs.next()) {
				return rs.getLong("rank");
			}
			return 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return 1;
		}
	}

	public long getTotalPlayerCount() {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) as total from user_table;");
			if (rs.next()) {
				return rs.getLong("total");
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
