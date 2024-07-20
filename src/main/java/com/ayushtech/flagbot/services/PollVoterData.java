package com.ayushtech.flagbot.services;

public class PollVoterData {
  private String userId;
  private String role;
  private boolean agree;
  public PollVoterData(String userId, String role, boolean agree) {
    this.userId = userId;
    this.role = role;
    this.agree = agree;
  }
  public String getUserId() {
    return this.userId;
  }
  public String getRole() {
    return this.role;
  }
  public boolean isAgree() {
    return this.agree;
  }
}
