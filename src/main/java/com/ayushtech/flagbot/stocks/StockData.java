package com.ayushtech.flagbot.stocks;

import java.util.concurrent.atomic.AtomicInteger;

public class StockData {

  private int value;
  private Company company;
  private AtomicInteger sold;
  private AtomicInteger bought;
  private int change;

  public StockData(Company company, int value) {
    this.value = value;
    this.company = company;
    this.sold = new AtomicInteger();
    this.bought = new AtomicInteger();
    change = 0;
  }

  public int getStockFluctuation() {
    int soldInt = sold.get();
    int boughtInt = bought.get();
    return getFluctuation(boughtInt - soldInt);
  }

  public void sellStock(int amount) {
    sold.addAndGet(amount);
  }

  public void buyStock(int amount) {
    bought.addAndGet(amount);
  }

  public void resetData() {
    sold.set(0);
    bought.set(0);
  }

  public Company getCompany() {
    return this.company;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  public void setChange(int change) {
    this.change = change;
  }

  public int getChange() {
    return this.change;
  }

  private static int getFluctuation(int difference) {
    if (difference == 0) {
      return 0;
    } else if (difference >= 10000) {
      return 100;
    } else if (difference <= -10000) {
      return -100;
    } else {
      int x = (int) Math.round(Math.sqrt(Math.abs(difference)));
      return difference > 0 ? x : x * -1;
    }
  }
}
