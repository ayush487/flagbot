package com.ayushtech.flagbot.services;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameEndService {
  private static GameEndService gameEndService = null;

  private ScheduledExecutorService executor;

  private GameEndService() {
    executor = new ScheduledThreadPoolExecutor(1);
  }

  public static GameEndService getInstance() {
    if (gameEndService == null) {
      gameEndService = new GameEndService();
    }
    return gameEndService;
  }

  public void scheduleEndGame(Runnable runnable, int duration, TimeUnit timeUnit) {
    executor.schedule(runnable, duration, timeUnit);
  }

}
