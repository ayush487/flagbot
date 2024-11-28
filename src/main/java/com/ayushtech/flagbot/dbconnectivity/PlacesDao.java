package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.game.location.LocationMap;
import com.ayushtech.flagbot.guessGame.place.Place;

public class PlacesDao {
  private static PlacesDao placesDao = null;

  private PlacesDao() {
  }

  public static PlacesDao getInstance() {
    if (placesDao == null) {
      placesDao = new PlacesDao();
    }
    return placesDao;
  }

  public List<String> getPlacesCodeList() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT code FROM places;");
      List<String> list = new ArrayList<>();
      while (rs.next()) {
        list.add(rs.getString("code"));
      }
      return list;
    } catch (SQLException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public List<Place> getPlacesList() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT code,name,location FROM places;");
      List<Place> places = new ArrayList<>();
      while (rs.next()) {
        places.add(new Place(rs.getString("code"), rs.getString("name"), rs.getString("location")));
      }
      return places;
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(0);
      return new ArrayList<>();
    }
  }

  public Place getPlace(String code) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(String.format("SELECT code,name,location FROM places where code='%s';", code));
      rs.next();
      return new Place(rs.getString("code"), rs.getString("name"), rs.getString("location"));
    } catch (SQLException e) {
      e.printStackTrace();
      return new Place(null, null, null);
    }
  }

  public LocationMap getRandomPlaceMap() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT name, correct FROM places_map_data ORDER BY RAND() LIMIT 1;");
      rs.next();
      return new LocationMap(rs.getString("name"), rs.getInt("correct"));
    } catch (SQLException e) {
      return new LocationMap("taj_6", 4);
    }
  }
}
