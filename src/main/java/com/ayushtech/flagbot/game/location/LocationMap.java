package com.ayushtech.flagbot.game.location;

public class LocationMap {
  private String code;
  private int correctAnswer;
  private String placeCode;

  public LocationMap(String code, int correctAnswer) {
    this.code = code;
    this.correctAnswer = correctAnswer;
    this.placeCode = code.split("_")[0];
  }

  public String getPlaceCode() {
    return this.placeCode;
  }

  public String getCode() {
    return code;
  }

  public int getCorrectAnswer() {
    return correctAnswer;
  }
}
