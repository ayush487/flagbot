package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegionDao {
	
	public static RegionDao regionDao = null;
	
	private RegionDao() {}
	
	
	public static synchronized RegionDao getInstance() {
		if(regionDao==null) {
			regionDao = new RegionDao();
		}
		return regionDao;
	}
	
	public String getRegion(String code) {
		try {
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement ps = conn.prepareStatement("select region from country where code2=?;");
			ps.setString(1, code);
			ResultSet rs = ps.executeQuery();
			String region = null;
			while(rs.next()) {
				region = rs.getString("region");
			}
			return region;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
