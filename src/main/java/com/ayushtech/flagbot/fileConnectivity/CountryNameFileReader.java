package com.ayushtech.flagbot.fileConnectivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CountryNameFileReader {
  private static CountryNameFileReader countryNameFileReader = null;

  private CountryNameFileReader() {
  }

  public static synchronized CountryNameFileReader getInstance() {
    if (countryNameFileReader == null) {
      countryNameFileReader = new CountryNameFileReader();
    }
    return countryNameFileReader;
  }

  public Map<String, Map<String, String>> getLangMap() {
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
    Map<String, String> arabicMap = new HashMap<>(305);
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(
              new FileInputStream("countries-names.csv"), StandardCharsets.UTF_8));

      CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
      String[] records;
      while ((records = csvReader.readNext()) != null) {
        dutchMap.put(records[0], records[2]);
        frenchMap.put(records[0], records[3]);
        germanMap.put(records[0], records[4]);
        japaneseMap.put(records[0], records[5]);
        koreanMap.put(records[0], records[6]);
        portugueseMap.put(records[0], records[7]);
        russianMap.put(records[0], records[8]);
        spanishMap.put(records[0], records[9]);
        swedishMap.put(records[0], records[10]);
        turkishMap.put(records[0], records[11]);
        arabicMap.put(records[0], records[12]);
      }
      csvReader.close();
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
    Map<String, Map<String, String>> languageMap = new HashMap<>(10);
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
    languageMap.put("arabic", arabicMap);
    return languageMap;
  }
}
