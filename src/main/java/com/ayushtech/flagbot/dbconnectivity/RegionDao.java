package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RegionDao {

	public static RegionDao regionDao = null;

	private RegionDao() {
	}

	public static synchronized RegionDao getInstance() {
		if (regionDao == null) {
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
			while (rs.next()) {
				region = rs.getString("region");
			}
			return region;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String[] getCountryData(String countryCode) {
		String[] data = new String[3];
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(
					"SELECT country_code, country_name, continent_code FROM country_continents WHERE country_code='%s';",
					countryCode));
			if (rs.next()) {
				data[0] = rs.getString("country_code");
				data[1] = rs.getString("country_name");
				data[2] = rs.getString("continent_code");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	public List<String> getCountryCodeList() {
		List<String> countryCodeList = new ArrayList<>();
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT country_code FROM country_continents;");
			while (rs.next()) {
				countryCodeList.add(rs.getString("country_code"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return countryCodeList;
	}
}
