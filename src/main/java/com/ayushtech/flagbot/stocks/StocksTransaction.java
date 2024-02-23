package com.ayushtech.flagbot.stocks;

import net.dv8tion.jda.api.utils.TimeFormat;

public class StocksTransaction {
  private long userId;
  private TransactionType type;
  private Company company;
  private int count;
  private long timeStamp;
  private int price;

  public StocksTransaction(long userId, TransactionType type, Company company, int count, long timeStamp, int price) {
    this.userId = userId;
    this.type = type;
    this.company = company;
    this.count = count;
    this.timeStamp = timeStamp;
    this.price = price;
  }

  public long getUserId() {
    return userId;
  }

  public String getType() {
    return type.toString();
  }

  public String getCompany() {
    return company.toString();
  }

  public int getCount() {
    return count;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public int getPrice() {
    return price;
  }

  public String getTransactionMessage() {
    String message = String.format(
        "You **%s** `%d` shares of **%s** on " + TimeFormat.DATE_TIME_SHORT.atTimestamp(timeStamp) + " `%d` :coin: each.\n",
        type.toString(),
        count, company.toString(), price);
    return message;
  }
}
