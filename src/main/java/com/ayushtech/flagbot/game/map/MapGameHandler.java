package com.ayushtech.flagbot.game.map;

import java.util.HashMap;
import java.util.Optional;

import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.PatreonService;

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
        if (gameHandler == null) {
            gameHandler = new MapGameHandler();
        }
        return gameHandler;
    }

    public HashMap<Long, MapGame> getGameMap() {
        return mapGameMap;
    }

    public boolean addGame(SlashCommandInteractionEvent event) {

        if (mapGameMap.containsKey(event.getChannel().getIdLong())) {
            event.getHook().sendMessage("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.getHook().sendMessage("Starting game now!").queue();
            OptionMapping difficultyOption = event.getOption("include_non_sovereign_countries");
            OptionMapping roundsOption = event.getOption("rounds");
            boolean isHard = difficultyOption == null ? false : difficultyOption.getAsBoolean();
            int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
            rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
            Optional<String> langOption = LanguageService.getInstance()
                    .getLanguageSelected(event.getGuild().getIdLong());
            MapGame game = new MapGame(event.getChannel(), isHard, rounds, rounds, langOption.orElse(null));
            mapGameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }

    public boolean addGame(ButtonInteractionEvent event) {
        if (mapGameMap.containsKey(event.getChannel().getIdLong())) {
            event.reply("There is already a game running in this channel!").queue();
            return false;
        } else {
            event.reply("Starting game now!").queue();
            String[] commandData = event.getComponentId().split("_");
            boolean isHard = commandData[1].equals("Hard");
            int rounds = Integer.parseInt(commandData[2]);
            Optional<String> langOption = LanguageService.getInstance()
                    .getLanguageSelected(event.getGuild().getIdLong());
            MapGame game = new MapGame(event.getChannel(), isHard, rounds, rounds, langOption.orElse(null));
            mapGameMap.put(event.getChannel().getIdLong(), game);
            return true;
        }
    }

    public void endGame(Long channelId) {
        if (mapGameMap.containsKey(channelId)) {
            mapGameMap.remove(channelId);
        }
    }

    public void handleGuess(String guessWord, MessageReceivedEvent event) {
        if (mapGameMap.containsKey(event.getChannel().getIdLong())) {
            Long channelId = event.getChannel().getIdLong();
            long authorId = event.getAuthor().getIdLong();
            if (mapGameMap.get(channelId).guess(guessWord)) {
                if (PatreonService.getInstance().hasUserCustomCorrectReactions(authorId)) {
                    try {
                        event.getMessage().addReaction(PatreonService.getInstance().getCorrectReaction(authorId))
                                .queue();
                    } catch (Exception e) {
                    }
                } else {
                    event.getMessage().addReaction("U+1F389").queue();
                }
                mapGameMap.get(channelId).endGameAsWin(event);
            } else {
                if (PatreonService.getInstance().hasUserCustomWrongReactions(authorId)) {
                    event.getMessage().addReaction(PatreonService.getInstance().getWrongReaction(authorId)).queue();
                }
            }
        }

    }
}
