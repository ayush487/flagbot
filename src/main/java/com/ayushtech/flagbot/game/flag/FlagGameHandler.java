package com.ayushtech.flagbot.game.flag;

import java.util.HashMap;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class FlagGameHandler {

    private static FlagGameHandler gameHandler = null;

    private HashMap<Long, FlagGame> gameMap;

    private FlagGameHandler() {
        gameMap = new HashMap<>();
    }

    public static synchronized FlagGameHandler getInstance() {
        if (gameHandler == null) {
            gameHandler = new FlagGameHandler();
        }
        return gameHandler;
    }

    public HashMap<Long, FlagGame> getGameMap() {
        return gameMap;
    }

    public boolean addGame(SlashCommandInteractionEvent event) {

        if (gameMap.containsKey(event.getChannel().getIdLong())) {
            event.getHook().sendMessage("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.getHook().sendMessage("Starting game now!").queue();
            OptionMapping difficultyOption = event.getOption("mode");
            OptionMapping roundsOption = event.getOption("rounds");
            String difficultyString = difficultyOption == null ? "sovereign countries only" : difficultyOption.getAsString().toLowerCase();
            byte difficulty = 0;
            if (difficultyString.startsWith("sovereign")) {
                difficulty = 0;
            } else if (difficultyString.startsWith("non")) {
                difficulty = 1;             
            } else if (difficultyString.startsWith("all")) {
                difficulty = 2;
            } else {
                difficulty = 0;
            }
            int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
            rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
            FlagGame game = new FlagGame(event.getChannel(), difficulty, rounds, rounds);
            gameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }

    public boolean addGame(ButtonInteractionEvent event) {
        if (gameMap.containsKey(event.getChannel().getIdLong())) {
            event.reply("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.reply("Starting game now!").queue();
            String[] commandData = event.getComponentId().split("_");
            byte difficulty = Byte.parseByte(commandData[1]);
            int rounds = Integer.parseInt(commandData[2]);
            FlagGame game = new FlagGame(event.getChannel(), difficulty, rounds, rounds);
            gameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }

    public void endGame(Long channelId) {
        if (gameMap.containsKey(channelId)) {
            gameMap.remove(channelId);
        }
    }

    public void handleGuess(String guessWord, MessageReceivedEvent event) {
        if (gameMap.containsKey(event.getChannel().getIdLong())) {
            Long channelId = event.getChannel().getIdLong();
            if (gameMap.get(channelId).guess(guessWord)) {
                event.getMessage().addReaction("U+1F389").queue();
                gameMap.get(channelId).endGameAsWin(event);
            }
        }

    }
}
