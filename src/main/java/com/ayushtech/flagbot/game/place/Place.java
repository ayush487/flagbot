package com.ayushtech.flagbot.game.place;

public class Place {
  private String code;
  private String name;
  private String location;
  public Place(String code, String name, String location) {
    this.code = code;
    this.name = name;
    this.location = location;
  }
  public String getCode() {
    return code;
  }
  public String getName() {
    return name;
  }
  public String getLocation() {
    return location;
  }
  
}
