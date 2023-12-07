package com.ayush.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelDao {

	public static ChannelDao channelDao = null;

	private ChannelDao() {
	}

	public static ChannelDao getInstance() {
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
//			e.printStackTrace();
			return false;
		}
	}
	
	public boolean enableChannel(Long channelId) {
		try {
			Connection conn = ConnectionProvider.getConnection(); //delete from disabled_channels where channel=1127236362999959594;
			PreparedStatement ps = conn.prepareStatement("Delete from disabled_channels where channel=?;");
			ps.setLong(1, channelId);
			ps.executeUpdate();
			return true;
		}catch(SQLException e) {
			return false;
		}
	}
}
