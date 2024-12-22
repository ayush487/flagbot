package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import com.ayushtech.flagbot.atlas.AtlasQuestion;

public class AtlasDao {
  private static AtlasDao atlasDao = null;
  private Random random;

  private AtlasDao() {
    random = new Random();
  };

  public static AtlasDao getInstance() {
    if (atlasDao == null) {
      atlasDao = new AtlasDao();
    }
    return atlasDao;
  }

  public AtlasQuestion getQuestion() {
    int qId = random.nextInt(375) + 1;
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT question, answers FROM atlas_quiz WHERE qId=" + qId + ";");
      if (rs.next()) {
        return new AtlasQuestion(qId, rs.getString("question"), rs.getString("answers"));
      }
      throw new RuntimeException();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
}