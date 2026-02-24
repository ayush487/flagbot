package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.utils.LbEntry;

public class LeaderboardDao {

	public List<LbEntry> getPlayersBasedOnCoins(int size, long offset) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(
					"SELECT user_id, coins, username from user_table order by coins desc limit %d offset %d;", size, offset));
			ArrayList<LbEntry> entries = new ArrayList<>();
			while (rs.next()) {
				var entry = new LbEntry(++offset, rs.getLong("user_id"), rs.getLong("coins"), rs.getString("username"));
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
					"SELECT user_id, level, username from user_table order by level desc limit %d offset %d;", size, offset));
			ArrayList<LbEntry> entries = new ArrayList<>();
			while (rs.next()) {
				var entry = new LbEntry(++offset, rs.getLong("user_id"), rs.getInt("level"), rs.getString("username"));
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

	public void updateUsernames(List<LbEntry> entries) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			for (LbEntry entry : entries) {
				stmt.addBatch(String.format("update user_table set username='%s' where user_id=%d;", entry.getName(), entry.getUserId()));
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
