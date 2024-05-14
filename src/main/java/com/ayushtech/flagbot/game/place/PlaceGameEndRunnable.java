package com.ayushtech.flagbot.game.place;

public class PlaceGameEndRunnable implements Runnable {

  private PlaceGame game;
  private Long channelId;

  public PlaceGameEndRunnable(PlaceGame game, Long channelId) {
    this.game = game;
    this.channelId = channelId;
  }

  @Override
  public void run() {
   if (PlaceGameHandler.getInstance().getGameMap().containsKey(channelId) &&
        PlaceGameHandler.getInstance().getGameMap().get(channelId) == game) {
      game.endGameAsLose();
    } 
  }
  
}
