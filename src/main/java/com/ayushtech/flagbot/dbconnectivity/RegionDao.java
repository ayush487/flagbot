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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ayushtech.flagbot.guessGame.Country;
import com.ayushtech.flagbot.guessGame.capital.Capital;

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
		try (Connection conn = ConnectionProvider.getConnection()) {
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

	public List<Capital> getCapitalList() {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT country,country_code,capital FROM capital;");
			List<Capital> capitalList = new ArrayList<>(194);
			while (rs.next()) {
				String countryCode = rs.getString("country_code");
				String countryName = rs.getString("country");
				String capitalName = rs.getString("capital");
				capitalList.add(new Capital(countryCode, countryName, capitalName));
			}
			return capitalList;
		} catch (SQLException e) {
			e.printStackTrace();
			return Stream.of(new Capital("IN", "INDIA", "New Delhi")).collect(Collectors.toList());
		}
	}

	public Map<String, String> getCountryCodeMap() {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT country_code,country_name FROM country_continents;");
			Map<String, String> countryCodeMap = new HashMap<>(285);
			while (rs.next()) {
				countryCodeMap.put(rs.getString(1), rs.getString(2));
			}
			return countryCodeMap;
		} catch (SQLException e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	public List<Country> getCountryList() {
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT country_code,country_name,continent_code,Sovereign FROM country_continents;");
			List<Country> countryList = new ArrayList<>();
			while (rs.next()) {
				String code = rs.getString("country_code");
				String continentCode = rs.getString("continent_code");
				String countryName = rs.getString("country_name");
				boolean isSovereign = rs.getBoolean("Sovereign");
				countryList.add(new Country(code, countryName, continentCode, isSovereign));
			}
			return countryList;
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}

	public Map<String, String> getLogoMap() {
		
		Map<String, String> logoMap = new HashMap<>(409);
		try (Connection conn = ConnectionProvider.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT code,name from brands;");
			while (rs.next()) {
				logoMap.put(rs.getString("code"), rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return logoMap;
	}

}