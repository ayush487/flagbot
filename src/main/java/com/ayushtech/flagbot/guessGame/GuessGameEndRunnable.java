package com.ayushtech.flagbot.guessGame;

public class GuessGameEndRunnable implements Runnable {

  private GuessGame guessGame;
  private long channelId;

  public GuessGameEndRunnable(GuessGame game, long channelId) {
    this.guessGame = game;
    this.channelId = channelId;
  }

  @Override
  public void run() {
   if (GuessGameHandler.getInstance().isActiveGame(channelId)) {
    GuessGameHandler.getInstance().requestEndGame(guessGame,channelId);
   }
  }
  
}
