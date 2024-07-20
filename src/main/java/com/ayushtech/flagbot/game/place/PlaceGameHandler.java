package com.ayushtech.flagbot.game.place;

import java.util.HashMap;
import java.util.Map;

import com.ayushtech.flagbot.services.PatreonService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlaceGameHandler {
  private static PlaceGameHandler placeGameHandler = null;
  private Map<Long, PlaceGame> gameMap;

  private PlaceGameHandler() {
    gameMap = new HashMap<>();
  }

  public static PlaceGameHandler getInstance() {
    if (placeGameHandler == null) {
      placeGameHandler = new PlaceGameHandler();
    }
    return placeGameHandler;
  }

  public boolean addGame(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue();
      return false;
    } else {
      event.getHook().sendMessage("Starting game now!").queue();
      OptionMapping roundsOption = event.getOption("rounds");
      int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
      rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
      PlaceGame game = new PlaceGame(event.getChannel(), rounds, rounds);
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
      int rounds = Integer.parseInt(commandData[1]);
      PlaceGame game = new PlaceGame(event.getChannel(), rounds, rounds);
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
      long authorId = event.getAuthor().getIdLong();
      if (gameMap.get(channelId).guess(guessWord)) {
        if (PatreonService.getInstance().hasUserCustomCorrectReactions(authorId)) {
          try {
            event.getMessage().addReaction(PatreonService.getInstance().getCorrectReaction(authorId))
                .queue();
          } catch (Exception e) {
          }
        } else {
          event.getMessage().addReaction("U+1F389").queue();
        }
        gameMap.get(channelId).endGameAsWin(event);
      } else {
        if (PatreonService.getInstance().hasUserCustomWrongReactions(authorId)) {
          event.getMessage().addReaction(PatreonService.getInstance().getWrongReaction(authorId)).queue();
        }
      }
    }
  }

  public Map<Long, PlaceGame> getGameMap() {
    return this.gameMap;
  }
}
