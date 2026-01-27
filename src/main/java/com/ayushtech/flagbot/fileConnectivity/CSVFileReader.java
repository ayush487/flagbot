package com.ayushtech.flagbot.fileConnectivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.ayushtech.flagbot.guessGame.state_flag.State;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CSVFileReader {
  private static CSVFileReader countryNameFileReader = null;

  private CSVFileReader() {
  }

  public static synchronized CSVFileReader getInstance() {
    if (countryNameFileReader == null) {
      countryNameFileReader = new CSVFileReader();
    }
    return countryNameFileReader;
  }

  public Map<String, Map<String, String>> getLangMap() {
    Map<String, String> spanishMap = new HashMap<>(290);
    Map<String, String> portugueseMap = new HashMap<>(290);
    Map<String, String> japaneseMap = new HashMap<>(290);
    Map<String, String> koreanMap = new HashMap<>(290);
    Map<String, String> turkishMap = new HashMap<>(290);
    Map<String, String> frenchMap = new HashMap<>(290);
    Map<String, String> russianMap = new HashMap<>(290);
    Map<String, String> swedishMap = new HashMap<>(290);
    Map<String, String> germanMap = new HashMap<>(290);
    Map<String, String> dutchMap = new HashMap<>(290);
    Map<String, String> arabicMap = new HashMap<>(290);
    Map<String, String> croatianMap = new HashMap<>(290);
    Map<String, String> thaiMap = new HashMap<>(290);
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
        croatianMap.put(records[0], records[13]);
        thaiMap.put(records[0], records[14]);
      }
      csvReader.close();
      reader.close();
    } catch (IOException e) {
      System.out.print("\n\n--------------------\nFailed to ready country-names.csv\nTerminating the Application\n----------------------\n");
      System.exit(0);
    }
    Map<String, Map<String, String>> languageMap = new HashMap<>(13);
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
    languageMap.put("croatian", croatianMap);
    languageMap.put("thai", thaiMap);
    return languageMap;
  }

  public Map<String, Map<String, State>> getStateMap() {
    Map<String, Map<String, State>> stateMap = new HashMap<>();
    stateMap.put("us", new HashMap<>());
    stateMap.put("br", new HashMap<>());
    stateMap.put("de", new HashMap<>());
    stateMap.put("es", new HashMap<>());
    stateMap.put("ch", new HashMap<>());
    stateMap.put("ca", new HashMap<>());
    stateMap.put("it", new HashMap<>());
    stateMap.put("ru", new HashMap<>());
    stateMap.put("nl", new HashMap<>());
    stateMap.put("en", new HashMap<>());
    stateMap.put("au", new HashMap<>());
    stateMap.put("jp", new HashMap<>());
    stateMap.put("pl", new HashMap<>());
    stateMap.put("ar", new HashMap<>());
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(
              new FileInputStream("states.csv"), StandardCharsets.UTF_8));
      CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
      String[] records;
      while ((records = csvReader.readNext()) != null) {
        State newState = new State(records[0], records[1], records[3]);
        if (!records[2].isBlank()) {
					newState.setAlternativeName(records[2]);
				}
        stateMap.get(records[3]).put(newState.getStateCode(), newState);
      }
      csvReader.close();
      reader.close();
    } catch (IOException e) {
      System.out.print("\n\n--------------------\nFailed to ready states.csv\nTerminating the Application\n----------------------\n");
      System.exit(0);
    }
    return stateMap;
  }
}
