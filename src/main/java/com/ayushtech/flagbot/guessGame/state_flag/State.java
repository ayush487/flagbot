package com.ayushtech.flagbot.guessGame.state_flag;

public class State {
  private String stateCode;
  private String name;
  private String country;
  private String alternativeName;

  public State(String stateCode, String name, String country) {
    this.stateCode = stateCode;
    this.name = name;
    this.country = country;
    this.alternativeName = null;
  }

  public String getStateCode() {
    return stateCode;
  }

  public String getName() {
    return name;
  }

  public void setAlternativeName(String alternativeName) {
    this.alternativeName = alternativeName;
  }

  public String getCountry() {
    return country;
  }

  public String getAlternativeName() {
    return alternativeName;
  }

  public String getFlag() {
    return String.format("https://raw.githubusercontent.com/ayush487/image-library/main/states/%s/%s.png", this.country,
        this.stateCode);
  }

  public boolean hasAlternativeName() {
    return this.alternativeName != null;
  }
}
