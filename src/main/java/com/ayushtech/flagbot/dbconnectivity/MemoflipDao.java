package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ayushtech.flagbot.memoflip.Difficulty;

public class MemoflipDao {
  private static MemoflipDao memoflipDao = null;

  private MemoflipDao() {
  }

  public static MemoflipDao getInstance() {
    if (memoflipDao == null) {
      memoflipDao = new MemoflipDao();
    }
    return memoflipDao;
  }

  public void setHighScore(long userId, int score, Difficulty difficulty) {
    
      Connection conn = ConnectionProvider.getConnection();
      try {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT %s from memoflip_scores where user_id=%d;", difficulty, userId));
        if (rs.next()) {
          int previousScore = rs.getInt(difficulty.name());
          if (previousScore <= score) {
            return;
          }
          stmt.executeUpdate(String.format("UPDATE memoflip_scores SET %s=%d where user_id=%d;", difficulty, score, userId));
          return;
        } else {
          stmt.executeUpdate(String.format("INSERT INTO memoflip_scores(user_id, %s) values(%d, %d);", difficulty, userId, score));
          return;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
  }

  public int[] getScores(long userId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(String.format("SELECT EASY,MEDIUM,HARD FROM memoflip_scores where user_id=%d;", userId));
      if (rs.next()) {
        int[] scores = new int[3];
        scores[0] = rs.getInt("easy");
        scores[1] = rs.getInt("medium");
        scores[2] = rs.getInt("hard");
        return scores;
      } else {
        return new int[]{9999,9999,99999};
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return new int[]{9999,9999,99999};
    }
  }
}
