package com.ayushtech.flagbot.dbconnectivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class LanguageDao {
  private static LanguageDao languageDao = null;

  private LanguageDao() {
  }

  public static LanguageDao getInstance() {
    if (languageDao == null) {
      languageDao = new LanguageDao();
    }
    return languageDao;
  }

  public Map<Long, String> getLanguageMap() {
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT server_id, lang from language_table;");
      Map<Long, String> languageMap = new HashMap<>();
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
      stmt.executeUpdate(
          String.format("Insert into language_table (server_id, lang) values (%d, '%s');", serverId, language));
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

  public Map<String, Map<String, String>> getLangMap() {
    Map<String, String> arabicMap = new HashMap<>(305);
    Map<String, String> spanishMap = new HashMap<>(305);
    Map<String, String> portugueseMap = new HashMap<>(305);
    Map<String, String> japaneseMap = new HashMap<>(305);
    Map<String, String> koreanMap = new HashMap<>(305);
    Map<String, String> turkishMap = new HashMap<>(305);
    Map<String, String> frenchMap = new HashMap<>(305);
    Map<String, String> russianMap = new HashMap<>(305);
    Map<String, String> swedishMap = new HashMap<>(305);
    Map<String, String> germanMap = new HashMap<>(305);
    Map<String, String> dutchMap = new HashMap<>(305);
    Connection conn = ConnectionProvider.getConnection();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT code,sw,ru,fr,tr,kr,ja,pt,es,ar,de,nl from country_names;");
      while (rs.next()) {
        String code = rs.getString("code");
        arabicMap.put(code, rs.getString("ar"));
        spanishMap.put(code, rs.getString("es"));
        portugueseMap.put(code, rs.getString("pt"));
        japaneseMap.put(code, rs.getString("ja"));
        koreanMap.put(code, rs.getString("kr"));
        turkishMap.put(code, rs.getString("tr"));
        frenchMap.put(code, rs.getString("fr"));
        russianMap.put(code, rs.getString("ru"));
        swedishMap.put(code, rs.getString("sw"));
        germanMap.put(code, rs.getString("de"));
        dutchMap.put(code, rs.getString("nl"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    Map<String, Map<String, String>> languageMap = new HashMap<>(10);
    languageMap.put("arabic", arabicMap);
    languageMap.put("spanish", spanishMap);
    languageMap.put("japanese", japaneseMap);
    languageMap.put("portuguese", portugueseMap);
    languageMap.put("korean", koreanMap);
    languageMap.put("turkish", turkishMap);
    languageMap.put("french", frenchMap);
    languageMap.put("russian", russianMap);
    languageMap.put("swedish", swedishMap);
    languageMap.put("german", germanMap);
    languageMap.put("dutch", dutchMap);
    return languageMap;
  }
}