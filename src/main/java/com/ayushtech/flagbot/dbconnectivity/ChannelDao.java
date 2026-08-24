package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.ayushtech.flagbot.services.ChannelService;

public class ChannelDao {

	public static ChannelDao channelDao = null;

	private ChannelDao() {
	}

	public static synchronized ChannelDao getInstance() {
		if (channelDao == null) {
			channelDao = new ChannelDao();
		}
		return channelDao;
	}

	public boolean isChannelDisabled(Long channelId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("select channel from disabled_channels where channel=?;");
			ps.setLong(1, channelId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addDisableChannel(Long channelId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("Insert into disabled_channels (channel) values (?);");
			ps.setLong(1, channelId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean enableChannel(Long channelId) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			PreparedStatement ps = conn.prepareStatement("Delete from disabled_channels where channel=?;");
			ps.setLong(1, channelId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addDisableChannel(List<Long> channelIdList) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement st = conn.createStatement();
			String query = getQueryToAddMultipleChannels(channelIdList);
			st.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<Long, String> getServerPrefixData() {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT server_id, prefix FROM servers;");
			Map<Long, String> serverPrefixData = new HashMap<>();
			while (rs.next())
				serverPrefixData.put(rs.getLong("server_id"), rs.getString("prefix"));
			return serverPrefixData;
		} catch (SQLException e) {
			return new HashMap<>();
		}
	}

	public boolean setServerPrefix(long serverId, String prefix) {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(
					"INSERT INTO servers (server_id, prefix) VALUES (%d, '%s') on duplicate key update prefix='%s';",
					serverId, prefix, prefix));
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public List<Long> getAllDisabledChannels() {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement st = conn.createStatement();
			ResultSet channelResultSet = st.executeQuery("select channel from disabled_channels;");
			List<Long> list = new ArrayList<>();
			while (channelResultSet.next()) {
				list.add(channelResultSet.getLong("channel"));
			}
			return list;
		} catch (Exception e) {
			System.out.println("Something went wrong while accesing database");
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public void addCommandUsageData(Map<Long, Map<String, AtomicInteger>> snapshot) {
		String sql = "INSERT INTO command_usage(user_id, command_name, use_count, last_used) VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE use_count = use_count + VALUES(use_count), last_used=NOW()";
		try (Connection conn = ConnectionProvider.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			for (var userEntry : snapshot.entrySet()) {
				for (var commandEntry : userEntry.getValue().entrySet()) {
					ps.setLong(1, userEntry.getKey());
					ps.setString(2, commandEntry.getKey());
					ps.setInt(3, commandEntry.getValue().get());
					ps.addBatch();
				}
			}
			ps.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String getQueryToAddMultipleChannels(List<Long> channelIdList) {
		String query = channelIdList.stream()
				.filter(cId -> !ChannelService.getInstance().isChannelDisabled(cId))
				.map(cid -> "(" + cid + ")")
				.collect(Collectors.joining(",", "Insert into disabled_channels (channel) values ", ";"));
		return query;
	}
}
