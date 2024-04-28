package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class LanguageDao {
  private static LanguageDao languageDao = null;
  private LanguageDao() {}
  public static LanguageDao getInstance() {
    if (languageDao==null) {
      languageDao = new LanguageDao();
    }
    return languageDao;
  }

  public Map<Long, String> getLanguageMap() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT server_id, lang from language_table;");
      Map<Long,String> languageMap = new HashMap<>();
      while (rs.next()) {
        languageMap.put(rs.getLong("server_id"), rs.getString("lang"));
      }
      return languageMap;
    } catch (SQLException e) {
      e.printStackTrace();
      return new HashMap<>();
    }
  }

  public void setGuildLanguage(long serverId, String language) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format("Insert into language_table (server_id, lang) values (%d, '%s');", serverId, language));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void removeGuildLanguage(long serverId) {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format("delete from language_table where server_id=%d;", serverId));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
