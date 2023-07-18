package com.ayush.game;

public class GameEndRunnable implements Runnable {

    private FlagGame flagGame;
    private long channelId;

    public GameEndRunnable(FlagGame flagGame, long channelId) {
        this.flagGame = flagGame;
        this.channelId = channelId;
    }

    @Override
    public void run() {
        if(GameHandler.getInstance().getGameMap().containsKey(channelId)
        && GameHandler.getInstance().getGameMap().get(channelId)==flagGame){
            flagGame.endGameAsLose();
        }
    }
    
}
