package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.services.PollVoterData;

public class PollsDao {

  public static PollsDao pollsDao = null;

  private PollsDao() {
  }

  public static PollsDao getInstance() {
    if (pollsDao == null) {
      pollsDao = new PollsDao();
    }
    return pollsDao;
  }

  public int[] upvotePoll(String pollId, String userId, String role) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      String rolecolumnName = role.equals("ADMIN") ? "admin_votes"
          : role.equals("MODERATOR") ? "mod_votes" : "staff_votes";
      stmt.executeUpdate(
          String.format("UPDATE polls SET %s = %s + 1 WHERE poll_id=%s;", rolecolumnName, rolecolumnName, pollId));
      stmt.executeUpdate(
          String.format("INSERT INTO poll_votes (user_id, role, poll_id, isagree) values (%s, '%s', %s, true);", userId,
              role, pollId));
      ResultSet rs = stmt.executeQuery(
          String.format("SELECT user_id, role, poll_id, isagree FROM poll_votes WHERE poll_id=%s;", pollId));
      return createVoterDataIntoArray(rs);
    } catch (SQLException exception) {
    }
    return new int[6];
  }

  public int[] downvotePoll(String pollId, String userId, String role) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(
          String.format("INSERT INTO poll_votes (user_id, role, poll_id, isagree) values (%s, '%s', %s, false);",
              userId,
              role, pollId));
      ResultSet rs = stmt.executeQuery(
          String.format("SELECT user_id, role, poll_id, isagree FROM poll_votes WHERE poll_id=%s;", pollId));
      return createVoterDataIntoArray(rs);
    } catch (SQLException exception) {
    }
    return new int[6];
  }

  private int[] createVoterDataIntoArray(ResultSet rs) {
    int[] voteData = new int[6];
    try {
      while (rs.next()) {
        boolean isAgree = rs.getBoolean("isagree");
        String role = rs.getString("role");
        if (role.equals("ADMIN")) {
          if (isAgree)
            voteData[0]++;
          else
            voteData[1]++;
        } else if (role.equals("MODERATOR")) {
          if (isAgree)
            voteData[2]++;
          else
            voteData[3]++;
        } else {
          if (isAgree)
            voteData[4]++;
          else
            voteData[5]++;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return voteData;
  }

  public List<PollVoterData> getPollVoterData(String pollId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt
          .executeQuery(String.format("SELECT user_id, role, isagree FROM poll_votes WHERE poll_id=%s;", pollId));
      List<PollVoterData> pollVoterList = new ArrayList<>();
      while (rs.next()) {
        String userId = rs.getString("user_id");
        String role = rs.getString("role");
        boolean isAgree = rs.getBoolean("isagree");
        PollVoterData pollVoterData = new PollVoterData(userId, role, isAgree);
        pollVoterList.add(pollVoterData);
      }
      return pollVoterList;
    } catch (SQLException e) {
    }
    return new ArrayList<>();
  }

  public int getCurrentPollId() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT MAX(poll_id)+1 as current_poll from polls;");
      if (rs.next()) {
        return rs.getInt("current_poll");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public void createPoll(int pollId, String pollText) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format("INSERT INTO polls (poll_id, poll_text) values (%d, '%s');", pollId, pollText));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean isUserVoted(String pollId, String userId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt
          .executeQuery(
              String.format("SELECT user_id FROM poll_votes WHERE poll_id=%s and user_id=%s;", pollId, userId));
      if (rs.next()) {
        return true;
      }
      return false;
    } catch (SQLException e) {
      e.printStackTrace();
      return true;
    }
  }

  public void removePollVote(String pollId, String userId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(
          String.format("SELECT role,isagree from poll_votes where poll_id=%s and user_id=%s;", pollId, userId));
      if (rs.next()) {
        boolean isAgree = rs.getBoolean("isagree");
        String role = rs.getString("role");
        if (isAgree) {
          String rolecolumnName = role.equals("ADMIN") ? "admin_votes"
              : role.equals("MODERATOR") ? "mod_votes" : "staff_votes";
          stmt.executeUpdate(
              String.format("UPDATE polls SET %s = %s - 1 WHERE poll_id=%s;", rolecolumnName, rolecolumnName, pollId));
        }
        stmt.executeUpdate(String.format("DELETE FROM poll_votes where poll_id=%s and user_id=%s;", pollId, userId));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
