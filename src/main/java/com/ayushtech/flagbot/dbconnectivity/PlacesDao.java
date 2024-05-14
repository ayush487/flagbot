package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.game.place.Place;

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

  public Place getPlace(String code) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(String.format("SELECT code,name,location FROM places where code='%s';", code));
      rs.next();
      return new Place(rs.getString("code"), rs.getString("name"), rs.getString("location"));
    } catch (SQLException e) {
      e.printStackTrace();
      return new Place(null,null,null);
    }
  }
}
