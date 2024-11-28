package com.ayushtech.flagbot.guessGame.place;

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

  public String getPlaceImage() {
    return String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", code);
  }

}
