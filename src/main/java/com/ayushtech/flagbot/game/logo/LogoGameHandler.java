package com.ayushtech.flagbot.game.logo;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LogoGameHandler {
  private static LogoGameHandler logoGameHandler = null;

  private HashMap<Long, LogoGame> gameMap;

  private LogoGameHandler() {
    gameMap = new HashMap<>();
  }

  public static synchronized LogoGameHandler getInstance() {
    if (logoGameHandler == null) {
      logoGameHandler = new LogoGameHandler();
    }
    return logoGameHandler;
  }

  public Map<Long, LogoGame> getGameMap() {
    return this.gameMap;
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
      LogoGame game = new LogoGame(event.getChannel(), rounds, rounds);
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
      LogoGame game = new LogoGame(event.getChannel(), rounds, rounds);
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
