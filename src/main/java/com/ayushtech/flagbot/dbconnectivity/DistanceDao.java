package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DistanceDao {
  private static DistanceDao distanceDao = null;

  private DistanceDao() {
  }

  public static synchronized DistanceDao getInstance() {
    if (distanceDao == null) {
      distanceDao = new DistanceDao();
    }
    return distanceDao;
  }

  public int[] getMapData(int randomMap) {
    int[] mapData = new int[4];
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(String.format("SELECT name,kms,miles,zoom FROM map_data WHERE name=%d;", randomMap));
      if (rs.next()) {
        mapData[0] = rs.getInt("name");
        mapData[1] = rs.getInt("kms");
        mapData[2] = rs.getInt("miles");
        mapData[3] = rs.getInt("zoom");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return mapData;
  }
}
