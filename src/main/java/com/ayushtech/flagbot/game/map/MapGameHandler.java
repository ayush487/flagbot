package com.ayushtech.flagbot.game.map;

import java.util.HashMap;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class MapGameHandler {

	private static MapGameHandler gameHandler = null;
	private HashMap<Long, MapGame> mapGameMap;
	
	private MapGameHandler() {
		mapGameMap = new HashMap<>();
	}
	
	public static synchronized MapGameHandler getInstance() {
		if(gameHandler==null) {
			gameHandler = new MapGameHandler();
		}
		return gameHandler;
	}
	
	public HashMap<Long, MapGame> getGameMap() {
        return mapGameMap;
    }
	
	public boolean addGame(SlashCommandInteractionEvent event) {
        
        if(mapGameMap.containsKey(event.getChannel().getIdLong())) {
            event.getHook().sendMessage("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.getHook().sendMessage("Starting game now!").queue();
            OptionMapping difficultyOption = event.getOption("include_non_soverign_countries");
            boolean isHard = difficultyOption == null ? false : difficultyOption.getAsBoolean();
            MapGame game = new MapGame(event.getChannel(), isHard);
            mapGameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }
    
    
    public boolean addGame(ButtonInteractionEvent event) {
        if(mapGameMap.containsKey(event.getChannel().getIdLong())) {
            event.reply("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.reply("Starting game now!").queue();
            boolean isHard = event.getComponentId().split("_")[1].equals("Hard");
            MapGame game = new MapGame(event.getChannel(), isHard);
            mapGameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }

    public void endGame(Long channelId) {
        if(mapGameMap.containsKey(channelId)) {
        	mapGameMap.remove(channelId);
        }
    }

    public void handleGuess(String guessWord, MessageReceivedEvent event) {
        if(mapGameMap.containsKey(event.getChannel().getIdLong())) {
            Long channelId = event.getChannel().getIdLong();
            if(mapGameMap.get(channelId).guess(guessWord)) {
                event.getMessage().addReaction("U+1F389").queue();
            	mapGameMap.get(channelId).endGameAsWin(event);
            } else {
                event.getMessage().addReaction("U+274C").queue();
            }
        }
        
    }


}
