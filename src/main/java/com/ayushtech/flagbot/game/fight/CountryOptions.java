package com.ayushtech.flagbot.game.fight;

public class CountryOptions {
  private CountryPair correctOption;
  private CountryPair[] options;
  public CountryOptions(CountryPair correctOption, CountryPair[] options) {
    this.correctOption = correctOption;
    this.options = options;
  }
  public CountryPair getCorrectOption() {
    return this.correctOption;
  }
  public CountryPair[] getOptions() {
    return this.options;
  }
}

class CountryPair {
  private String isoCode;
  private String name;
  public CountryPair(String isoCode, String name) {
    this.isoCode = isoCode;
    this.name = name;
  }
  public String getIsoCode() {
    return this.isoCode;
  }
  public String getName() {
    return this.name;
  }
}
