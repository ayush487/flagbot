package com.ayushtech.flagbot.distanceGuess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DistanceRound {
  private int code;
  private int distance;
  private int zoom;
  private Map<Long, Integer> userGuesses;

  public DistanceRound(int code, int distance, int zoom, int playerCount) {
    this.code = code;
    this.distance = distance;
    this.zoom = zoom;
    this.userGuesses = new HashMap<>();
  }


  public long[] getTop3Players() {
    if (userGuesses.size() <= 0) {
      return new long[] { 0l, 0l, 0l };
    }
    List<Long> userListRanked = userGuesses.keySet().stream()
    .sorted((uId1, uId2) -> Math.abs(userGuesses.get(uId1) - distance) - Math.abs(userGuesses.get(uId2) - distance))
        .collect(Collectors.toList());
    long[] top3 = new long[3];
    if (userListRanked.size() >= 3) {
      top3[0] = userListRanked.get(0);
      top3[1] = userListRanked.get(1);
      top3[2] = userListRanked.get(2);
    } else if (userListRanked.size() == 2) {
      top3[0] = userListRanked.get(0);
      top3[1] = userListRanked.get(1);
      top3[2] = 0l;
    } else {
      top3[0] = userListRanked.get(0);
      top3[1] = 0;
      top3[2] = 0;
    }
    return top3;
  }

  public void addGuess(long userId, int guess) {
    userGuesses.put(userId, guess);
  }

  public int getCode() {
    return this.code;
  }

  public int getDistance() {
    return this.distance;
  }

  public int getZoom() {
    return this.zoom;
  }

  public boolean isUserAlreadyGuessed(long userId) {
    return userGuesses.containsKey(userId);
  }

  public int getGuess(long userId) {
    return userGuesses.get(userId);
  }
}
