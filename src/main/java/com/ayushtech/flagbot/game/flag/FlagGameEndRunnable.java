package com.ayushtech.flagbot.game.flag;

public class FlagGameEndRunnable implements Runnable {

    private FlagGame game;
    private long channelId;

    public FlagGameEndRunnable(FlagGame game, long channelId) {
        this.game = game;
        this.channelId = channelId;
    }

    @Override
    public void run() {
        if(FlagGameHandler.getInstance().getGameMap().containsKey(channelId)
        && FlagGameHandler.getInstance().getGameMap().get(channelId)==game){
            game.endGameAsLose();
        }
    }
}
