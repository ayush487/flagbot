package com.ayushtech.flagbot.dbconnectivity;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		try {
			Connection conn = ConnectionProvider.getConnection();
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
		try {
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement ps = conn.prepareStatement("Insert into disabled_channels (channel) values (?);");
			ps.setLong(1, channelId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public boolean enableChannel(Long channelId) {
		try {
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement ps = conn.prepareStatement("Delete from disabled_channels where channel=?;");
			ps.setLong(1, channelId);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public boolean addDisableChannel(List<Long> channelIdList) {
		try {
			Connection conn = ConnectionProvider.getConnection();
			Statement st = conn.createStatement();
			String query = getQueryToAddMultipleChannels(channelIdList);
			st.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			return false;
		}

	}

	private String getQueryToAddMultipleChannels(List<Long> channelIdList) {
		StringBuilder builder = new StringBuilder("Insert into disabled_channels (channel) values ");
		channelIdList.forEach(channelId -> builder.append("(" + channelId + ")"));
		builder.append(";");
		return null;
	}
}
