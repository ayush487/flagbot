package com.ayush.game.map;

public class MapGameEndRunnable implements Runnable {
	
	private MapGame game;
	private long channelId;

	
	public MapGameEndRunnable(MapGame game, long channelId) {
		this.game = game;
		this.channelId = channelId;
	}

	@Override
	public void run() {
		if(MapGameHandler.getInstance().getGameMap().containsKey(channelId)
		        && MapGameHandler.getInstance().getGameMap().get(channelId)==game){
		            game.endGameAsLose();
		        }
	}

}
