package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

	public List<Long> getAllDisabledChannels() {
		try {
			Connection conn = ConnectionProvider.getConnection();
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

	private String getQueryToAddMultipleChannels(List<Long> channelIdList) {
		String query = channelIdList.stream()
				.filter(cId -> !ChannelService.getInstance().isChannelDisabled(cId))
				.map(cid -> "(" + cid + ")")
				.collect(Collectors.joining(",", "Insert into disabled_channels (channel) values ", ";"));
		return query;
	}
}
