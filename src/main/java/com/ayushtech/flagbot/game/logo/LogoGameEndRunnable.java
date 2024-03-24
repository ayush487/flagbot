package com.ayushtech.flagbot.game.logo;

public class LogoGameEndRunnable implements Runnable {

  private LogoGame game;
  private long channelId;

  public LogoGameEndRunnable(LogoGame game, long channelId) {
    this.game = game;
    this.channelId = channelId;
  }

  @Override
  public void run() {
    if (LogoGameHandler.getInstance().getGameMap().containsKey(channelId) &&
        LogoGameHandler.getInstance().getGameMap().get(channelId) == game) {
      game.endGameAsLose();
    }
  }

}
