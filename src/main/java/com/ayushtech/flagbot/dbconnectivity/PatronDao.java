package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

public class PatronDao {
  private static PatronDao patronDao = null;

  private PatronDao() {
  }

  public static synchronized PatronDao getInstance() {
    if (patronDao == null) {
      patronDao = new PatronDao();
    }
    return patronDao;
  }

  public HashSet<Long> getValidPatronMembers() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(
          String.format("Select user_id from patreon_members where validtill > %d", System.currentTimeMillis()));
      HashSet<Long> patrons = new HashSet<>();
      while (rs.next()) {
        patrons.add(rs.getLong("user_id"));
      }
      return patrons;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return new HashSet<>();
  }

  public HashMap<Long, String> getWrongReactions() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(
          "SELECT user_id, wrong_reaction,wrong_reaction_name FROM patreon_members WHERE wrong_reaction!=0;");
      HashMap<Long, String> map = new HashMap<>();
      while (rs.next()) {
        long uId = rs.getLong("user_id");
        long eId = rs.getLong("wrong_reaction");
        String eName = rs.getString("wrong_reaction_name");
        map.put(uId, eName + ":" + eId);
      }
      return map;
    } catch (SQLException e) {
      e.printStackTrace();
      return new HashMap<>();
    }
  }

  public HashMap<Long, String> getCorrectReactions() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt
          .executeQuery(
              "SELECT user_id, correct_reaction,correct_reaction_name FROM patreon_members where correct_reaction!=0;");
      HashMap<Long, String> map = new HashMap<>();
      while (rs.next()) {
        long uId = rs.getLong("user_id");
        long eId = rs.getLong("correct_reaction");
        String eName = rs.getString("correct_reaction_name");
        map.put(uId, eName + ":" + eId);
      }
      return map;
    } catch (SQLException e) {
      e.printStackTrace();
      return new HashMap<>();
    }
  }

  public long addNewPatron(long userId) {
    long duration = 2678400000l;
    Connection connection = ConnectionProvider.getConnection();
    try {
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("Select validtill from patreon_members where user_id=" + userId);
      if (rs.next()) {
        long validTill = rs.getLong("validtill");
        long currentTime = System.currentTimeMillis();
        if (validTill > currentTime) {
          stmt.executeUpdate(String.format("UPDATE patreon_members SET validtill = validtill + %d where user_id=%d",
              duration, userId));
          return validTill + duration;
        } else {
          stmt.executeUpdate(String.format("UPDATE patreon_members SET validtill = %d where user_id=%d",
              currentTime + duration, userId));
          return currentTime + duration;
        }
      } else {
        stmt.executeUpdate(
            String.format("INSERT INTO patreon_members(user_id, validtill) values(%d, %d)", userId,
                System.currentTimeMillis() + duration));
        return duration + System.currentTimeMillis();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return System.currentTimeMillis();
    }
  }

  public void setCorrectReaction(long authorId, long emoteId, String emoteName) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(
          String.format("UPDATE patreon_members SET correct_reaction=%d,correct_reaction_name='%s' WHERE user_id=%d;",
              emoteId, emoteName, authorId));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void setWrongReaction(long authorId, long emoteId, String emoteName) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(
          String.format("UPDATE patreon_members SET wrong_reaction=%d,wrong_reaction_name='%s' WHERE user_id=%d;",
              emoteId, emoteName, authorId));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void removeWrongReaction(long authorId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(String
          .format("UPDATE patreon_members SET wrong_reaction=0,wrong_reaction_name=null WHERE user_id=%d;", authorId));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public long getUserPatreonValidity(long userId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT validtill from patreon_members where user_id=" + userId + ";");
      if (rs.next()) {
        return rs.getLong("validtill");
      }
      return 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return 0;
    }
  }
}
