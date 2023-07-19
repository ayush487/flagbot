package com.ayush.game;

import java.util.HashMap;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GameHandler {

    private static GameHandler gameHandler = null;
    
    private HashMap<Long, FlagGame> gameMap;

    private GameHandler () {
        gameMap = new HashMap<>();
    }

    public static synchronized GameHandler getInstance() {
        if(gameHandler==null) {
            gameHandler = new GameHandler();
        }
        return gameHandler;
    }

    public HashMap<Long, FlagGame> getGameMap() {
        return gameMap;
    }

    public boolean addGame(SlashCommandInteractionEvent event) {
        
        if(gameMap.containsKey(event.getChannel().getIdLong())) {
            event.reply("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.reply("Starting game now!").queue();
            FlagGame game = new FlagGame(event.getChannel());
            gameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }
    
    public boolean addGame(ButtonInteractionEvent event) {
        if(gameMap.containsKey(event.getChannel().getIdLong())) {
            event.reply("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.reply("Starting game now!").queue();
            FlagGame game = new FlagGame(event.getChannel());
            gameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }

    public void endGame(Long channelId) {
        if(gameMap.containsKey(channelId)) {
            gameMap.remove(channelId);
        }
    }

    public void handleGuess(String guessWord, MessageReceivedEvent event) {
        if(gameMap.containsKey(event.getChannel().getIdLong())) {
            Long channelId = event.getChannel().getIdLong();
            if(gameMap.get(channelId).guess(guessWord)) {
                gameMap.get(channelId).endGameAsWin(event);
                event.getMessage().addReaction("U+1F389").queue();
            } else {
                event.getMessage().addReaction("U+274C").queue();
            }
        }
        
    }
}
