package com.ayushtech.flagbot.race;

public class CountryPair {
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