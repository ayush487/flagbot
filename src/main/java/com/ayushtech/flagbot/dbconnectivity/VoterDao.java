package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class VoterDao {

  private static VoterDao voterDao = null;

  private VoterDao() {
  }

  public static synchronized VoterDao getInstance() {
    if (voterDao == null) {
      voterDao = new VoterDao();
    }
    return voterDao;
  }

  public void addVoter(long voterId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format("INSERT INTO vote_data (voter_id, time) values (%d, %d);", voterId,
          System.currentTimeMillis()));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public Map<Long, Long> getRecentVoterData() {
    Connection conn = ConnectionProvider.getConnection();
    Map<Long, Long> voterData = new HashMap<>();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(String.format("SELECT voter_id, time FROM vote_data WHERE time > %d;",
          System.currentTimeMillis() - 86400000));
      while (rs.next()) {
        long voterId = rs.getLong("voter_id");
        long time = rs.getLong("time");
        if (voterData.containsKey(voterId)) {
          long prevTime = voterData.get(voterId);
          if (prevTime < time) {
            voterData.put(voterId, time);
          }
        } else {
          voterData.put(voterId, time);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return voterData;
  }
}
