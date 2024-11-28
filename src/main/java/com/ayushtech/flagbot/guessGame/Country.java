package com.ayushtech.flagbot.guessGame;

public class Country {
  private String code;
  private String name;
  private String continentCode;
  private boolean isSovereign;
  public Country(String code, String name, String continentCode, boolean isSovereign) {
    this.code = code;
    this.name = name;
    this.continentCode = continentCode;
    this.isSovereign = isSovereign;
  }
  public String getCode() {
    return code;
  }
  public String getName() {
    return name;
  }
  public String getContinentCode() {
    return continentCode;
  }
  public boolean isSovereign() {
    return isSovereign;
  }
  public String getFlagImage() {
    return "https://raw.githubusercontent.com/ayush487/image-library/main/flags/" + code + ".png"; 
  }
}
