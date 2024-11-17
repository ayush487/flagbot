package com.ayushtech.flagbot.game.capital;

public class Capital {
  private String countryCode;
  private String country;
  private String capital;

  public Capital(String countryCode, String country, String capital) {
    this.countryCode = countryCode;
    this.country = country;
    this.capital = capital;
  }

  public String getCountryCode() {
    return countryCode.toLowerCase();
  }

  public String getCountry() {
    return country;
  }

  public String getCapital() {
    return capital;
  }
}