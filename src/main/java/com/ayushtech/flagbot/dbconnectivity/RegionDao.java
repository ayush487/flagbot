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
import com.ayushtech.flagbot.guessGame.state_flag.State;

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

	public String getRandomCountryByContinent(String continentCode) {
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(
					"SELECT country_code FROM country_continents WHERE continent_code ='%s' ORDER by RAND() limit 1;",
					continentCode));
			if (rs.next()) {
				return rs.getString("country_code");
			}
			return "in";
		} catch (SQLException e) {
			e.printStackTrace();
			return "in";
		}
	}

	public List<Capital> getCapitalList() {
		Connection conn = ConnectionProvider.getConnection();
		try {
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
		Connection conn = ConnectionProvider.getConnection();
		try {
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
		Connection conn = ConnectionProvider.getConnection();
		try {
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
		Connection conn = ConnectionProvider.getConnection();
		Map<String, String> logoMap = new HashMap<>(409);
		try {
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

	public Map<String, Map<String, State>> getStateMap() {
		Map<String, Map<String, State>> stateMap = new HashMap<>();
		stateMap.put("us", new HashMap<>());
		stateMap.put("br", new HashMap<>());
		stateMap.put("de", new HashMap<>());
		stateMap.put("es", new HashMap<>());
		stateMap.put("ch", new HashMap<>());
		stateMap.put("ca", new HashMap<>());
		stateMap.put("it", new HashMap<>());
		stateMap.put("ru", new HashMap<>());
		stateMap.put("nl", new HashMap<>());
		stateMap.put("en", new HashMap<>());
		Connection conn = ConnectionProvider.getConnection();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT state_code,state_name,country_code,alternative_name FROM states;");
			while (rs.next()) {
				String countryCode = rs.getString("country_code");
				State state = new State(rs.getString("state_code"), rs.getString("state_name"), countryCode);
				if (rs.getString("alternative_name") != null) {
					state.setAlternativeName(rs.getString("alternative_name"));
				}
				stateMap.get(countryCode).put(state.getStateCode(), state);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return stateMap;
	}
}
