package com.ayushtech.flagbot.race;

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