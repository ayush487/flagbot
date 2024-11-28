package com.ayushtech.flagbot.guessGame.capital;

public class Capital {
  private String countryCode;
  private String country;
  private String capital;

  public Capital(String countryCode, String country, String capital) {
    this.countryCode = countryCode;
    this.country = country;
    this.capital = capital;
  }

  public String getFlagLink() {
    return "https://raw.githubusercontent.com/ayush487/image-library/main/flags/" + countryCode.toLowerCase() + ".png"; 
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