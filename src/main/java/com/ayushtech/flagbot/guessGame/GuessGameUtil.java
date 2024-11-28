package com.ayushtech.flagbot.guessGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.ayushtech.flagbot.dbconnectivity.PlacesDao;
import com.ayushtech.flagbot.dbconnectivity.RegionDao;
import com.ayushtech.flagbot.guessGame.capital.Capital;
import com.ayushtech.flagbot.guessGame.place.Place;

public class GuessGameUtil {

  private static GuessGameUtil gameUtil = null;

  private Random random;

  private List<String> brandCodeList;
  private List<Capital> capitalList;
  private List<Country> countryList;
  private List<Place> placeList;
  private Set<String> ignoreSetMapGuess;
  private Map<String, String> continentMap;
  private Map<String, String> continentCodeMap;
  private Map<String, String> brandMap;
  private Map<String, String> mapImageOverrideMap;
  private Map<String, String> alternativeCountryNames = new HashMap<>(19);
  private Map<String,String> countryCodeMap;

  private GuessGameUtil() {
    random = new Random();
    ignoreSetMapGuess = new HashSet<>();
    continentMap = new HashMap<>(7);
    mapImageOverrideMap = new HashMap<>(13);
    continentCodeMap = new HashMap<>(7);
    capitalList = RegionDao.getInstance().getCapitalList();
    countryList = RegionDao.getInstance().getCountryList();
    brandMap = RegionDao.getInstance().getLogoMap();
    placeList = PlacesDao.getInstance().getPlacesList();
    countryCodeMap = RegionDao.getInstance().getCountryCodeMap();
    brandCodeList = new ArrayList<>(brandMap.keySet());
    loadMapDataStatically();
  }

  public static GuessGameUtil getInstance() {
    if (gameUtil == null) {
      gameUtil = new GuessGameUtil();
    }
    return gameUtil;
  }

  public Capital getRandomCapital() {
    return capitalList.get(random.nextInt(capitalList.size()));
  }

  public Country getRandomCountry() {
    return countryList.get(random.nextInt(capitalList.size()));
  }

  public Country getRandomCountry(boolean isSovereign) {
    Country country = countryList.get(random.nextInt(countryList.size()));
    if (isSovereign) {
      while (!country.isSovereign()) {
        country = countryList.get(random.nextInt(countryList.size()));
      }
    } else {
      while (country.isSovereign()) {
        country = countryList.get(random.nextInt(countryList.size()));
      }
    }
    return country;
  }

  public Country getRandomCountryForMapGuess(boolean isSovereign) {
    if (isSovereign) {
      return getRandomCountry(true);
    }
    Country country = countryList.get(random.nextInt(countryList.size()));
    while (ignoreSetMapGuess.contains(country.getCode())) {
      country = countryList.get(random.nextInt(countryList.size()));
    }
    return country;
  }

  public Place getRandomPlace() {
    return placeList.get(random.nextInt(placeList.size()));
  }

  public String getMapImage(String countryName) {
    countryName = mapImageOverrideMap.containsKey(countryName)
        ? mapImageOverrideMap.get(countryName)
        : countryName.toLowerCase().replace(' ', '_');
    return String.format("https://maps.lib.utexas.edu/maps/cia16/%s_sm_2016.gif", countryName);
  }

  public Country getRandomCountry(String continentCode) {
    Country country = countryList.get(random.nextInt(countryList.size()));
    while (!country.getContinentCode().equals(continentCode)) {
      country = countryList.get(random.nextInt(countryList.size()));
    }
    return country;
  }

  public String getContinentName(String code) {
    if (code.equals("all")) {
      return "Not Specified";
    }
    return this.continentMap.get(code);
  }

  public String getRandomBrandCode() {
    return brandCodeList.get(random.nextInt(brandCodeList.size()));
  }

  public String getBrandName(String brandCode) {
    return brandMap.get(brandCode);
  }

  public boolean hasAlternativeName(String countryCode) {
    return alternativeCountryNames.containsKey(countryCode);
  }

  public String getAlternativeNames(String countryCode) {
    return alternativeCountryNames.getOrDefault(countryCode, "no alternative names");
  }

  public String getCountryName(String countryCode) {
    return countryCodeMap.get(countryCode);
  }

  public boolean isValidContinent(String continent) {
    return continentCodeMap.containsKey(continent);
  }

  public String getContinentCode(String continent) {
    return continentCodeMap.get(continent);
  }

  public String[] getCodeOptions() {
    int[] opt = get4randomNumbers(brandCodeList.size());
    String[] codeOptions = new String[4];
    codeOptions[0] = brandCodeList.get(opt[0]);
    codeOptions[1] = brandCodeList.get(opt[1]);
    codeOptions[2] = brandCodeList.get(opt[2]);
    codeOptions[3] = brandCodeList.get(opt[3]);
    return codeOptions;
  }

  public String[] getNameOptions(String[] codeOptions) {
    String[] nameOptions = new String[4];
    nameOptions[0] = brandMap.get(codeOptions[0]);
    nameOptions[1] = brandMap.get(codeOptions[1]);
    nameOptions[2] = brandMap.get(codeOptions[2]);
    nameOptions[3] = brandMap.get(codeOptions[3]);
    return nameOptions;
  }

  private int[] get4randomNumbers(int range) {
    int[] numbers = new int[4];
    numbers[0] = random.nextInt(range);
    do {
      numbers[1] = random.nextInt(range);
    } while (numbers[1] == numbers[0]);
    do {
      numbers[2] = random.nextInt(range);
    } while (numbers[2] == numbers[1] || numbers[2] == numbers[0]);
    do {
      numbers[3] = random.nextInt(range);
    } while (numbers[3] == numbers[2] || numbers[3] == numbers[1] || numbers[3] == numbers[0]);
    return numbers;
  }

  private void loadMapDataStatically() {
    ignoreSetMapGuess.add("gm");
    ignoreSetMapGuess.add("va");
    ignoreSetMapGuess.add("sz");
    ignoreSetMapGuess.add("ps");
    ignoreSetMapGuess.add("cd");
    ignoreSetMapGuess.add("aa");
    ignoreSetMapGuess.add("ac");
    ignoreSetMapGuess.add("ag");
    ignoreSetMapGuess.add("re");
    ignoreSetMapGuess.add("ax");
    ignoreSetMapGuess.add("bq");
    ignoreSetMapGuess.add("sh");
    ignoreSetMapGuess.add("sj");
    ignoreSetMapGuess.add("um");
    ignoreSetMapGuess.add("gf");
    ignoreSetMapGuess.add("gp");
    ignoreSetMapGuess.add("gs");
    ignoreSetMapGuess.add("gb-sct");
    ignoreSetMapGuess.add("gb-nir");
    ignoreSetMapGuess.add("yt");
    ignoreSetMapGuess.add("gb-wls");
    ignoreSetMapGuess.add("gb-eng");
    ignoreSetMapGuess.add("mq");
    ignoreSetMapGuess.add("ay");
    ignoreSetMapGuess.add("aj");
    ignoreSetMapGuess.add("alr");
    ignoreSetMapGuess.add("ab");
    ignoreSetMapGuess.add("ald");
    ignoreSetMapGuess.add("bas");
    ignoreSetMapGuess.add("bia");
    ignoreSetMapGuess.add("bri");
    ignoreSetMapGuess.add("bur");
    ignoreSetMapGuess.add("cri");
    ignoreSetMapGuess.add("che");
    ignoreSetMapGuess.add("chu");
    ignoreSetMapGuess.add("crm");
    ignoreSetMapGuess.add("dag");
    ignoreSetMapGuess.add("eai");
    ignoreSetMapGuess.add("her");
    ignoreSetMapGuess.add("ing");
    ignoreSetMapGuess.add("kpr");
    ignoreSetMapGuess.add("kab");
    ignoreSetMapGuess.add("kal");
    ignoreSetMapGuess.add("kar");
    ignoreSetMapGuess.add("kkp");
    ignoreSetMapGuess.add("krl");
    ignoreSetMapGuess.add("kha");
    ignoreSetMapGuess.add("kom");
    ignoreSetMapGuess.add("lad");
    ignoreSetMapGuess.add("mar");
    ignoreSetMapGuess.add("mor");
    ignoreSetMapGuess.add("nag");
    ignoreSetMapGuess.add("nos");
    ignoreSetMapGuess.add("rsr");
    ignoreSetMapGuess.add("bqu");
    ignoreSetMapGuess.add("sak");
    ignoreSetMapGuess.add("sar");
    ignoreSetMapGuess.add("sea");
    ignoreSetMapGuess.add("som");
    ignoreSetMapGuess.add("udm");
    ignoreSetMapGuess.add("tuv");
    ignoreSetMapGuess.add("ncy");
    ignoreSetMapGuess.add("tra");
    ignoreSetMapGuess.add("tdf");
    ignoreSetMapGuess.add("tat");
    continentMap.put("as", "Asia");
    continentMap.put("af", "Africa");
    continentMap.put("an", "Antarctica");
    continentMap.put("eu", "Europe");
    continentMap.put("oc", "Oceania");
    continentMap.put("sa", "South America");
    continentMap.put("na", "North America");
    continentCodeMap.put("asia", "as");
    continentCodeMap.put("africa", "af");
    continentCodeMap.put("europe", "eu");
    continentCodeMap.put("oceania", "oc");
    continentCodeMap.put("north america", "na");
    continentCodeMap.put("south america", "sa");
    continentCodeMap.put("antarctica", "an");
    alternativeCountryNames.put("ae", "UAE");
    alternativeCountryNames.put("ax", "Aland Islands");
    alternativeCountryNames.put("cd", "DR Congo, DRC");
    alternativeCountryNames.put("ci", "Côte d'Ivoire");
    alternativeCountryNames.put("cv", "Cabo Verde");
    alternativeCountryNames.put("cz", "Czech Republic");
    alternativeCountryNames.put("tr", "Turkey");
    alternativeCountryNames.put("us", "USA, United States");
    alternativeCountryNames.put("gb", "UK");
    alternativeCountryNames.put("tl", "East Timor");
    alternativeCountryNames.put("in", "Bharat");
    alternativeCountryNames.put("ba", "Bosnia");
    alternativeCountryNames.put("mm", "Burma");
    alternativeCountryNames.put("eu", "EU");
    alternativeCountryNames.put("cf", "CAR");
    alternativeCountryNames.put("gs", "South Georgia");
    alternativeCountryNames.put("sea", "Sealand");
    alternativeCountryNames.put("ncy", "Northern Cyprus");
    alternativeCountryNames.put("vi", "US Virgin Islands, United States Virgin Islands");
    mapImageOverrideMap.put("Myanmar", "burma");
    mapImageOverrideMap.put("North Macedonia", "macedonia");
    mapImageOverrideMap.put("South Korea", "korea_south");
    mapImageOverrideMap.put("North Korea", "korea_north");
    mapImageOverrideMap.put("Guinea-Bissau", "guinea_bissau");
    mapImageOverrideMap.put("Micronesia", "micronesia_federated_states_of");
    mapImageOverrideMap.put("United States of America", "united_states");
    mapImageOverrideMap.put("Turkiye", "turkey");
    mapImageOverrideMap.put("Cape Verde", "cabo_verde");
    mapImageOverrideMap.put("Ivory Coast", "cote_divoire");
    mapImageOverrideMap.put("Congo", "congo_republic_of_the");
    mapImageOverrideMap.put("Czechia", "czech_republic");
    mapImageOverrideMap.put("U.S. Virgin Islands", "virgin_islands");
  }
}
