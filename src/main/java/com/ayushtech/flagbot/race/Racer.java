package com.ayushtech.flagbot.race;

public class Racer {
  private String name;
  private int distance;
  private String car;

  public Racer(String name, String car) {
    this.name = name;
    this.distance = 0;
    this.car = car;
  }

  public String getName() {
    return this.name;
  }

  public int getDistance() {
    return this.distance;
  }

  public String getCar() {
    return this.car;
  }

  public boolean addDistance(int d) {
    if (d > 0) {
      distance = distance + d >= 100 ? 100 : distance + d;
    } else {
      distance = distance + d <= 0 ? 0 : distance + d;
    }
    return distance >= 100 ? true : false;
  }

}
