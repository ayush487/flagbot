package com.ayushtech.flagbot.game.capital;

public class CapitalGameEndRunnable implements Runnable {

  private CapitalGame capitalGame;
  private long channelId;

  public CapitalGameEndRunnable(CapitalGame capitalGame, long channelId) {
    this.capitalGame = capitalGame;
    this.channelId = channelId;
  }

  @Override
  public void run() {
    if (CapitalGameHandler.getInstance().getGameMap().containsKey(channelId) &&
        CapitalGameHandler.getInstance().getGameMap().get(channelId) == capitalGame) {
      CapitalGameHandler.getInstance().getGameMap().remove(channelId);
      capitalGame.endGameAsLose();
    }
  }

}
