package com.ayushtech.flagbot.guessGame;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.guessGame.capital.CapitalGuessGame;
import com.ayushtech.flagbot.guessGame.flag.FlagGuessGame;
import com.ayushtech.flagbot.guessGame.logo.LogoGuessGame;
import com.ayushtech.flagbot.guessGame.map.MapGuessGame;
import com.ayushtech.flagbot.guessGame.place.PlaceGuessGame;
import com.ayushtech.flagbot.guessGame.state_flag.StateFlagGuessGame;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.LanguageService;
import com.ayushtech.flagbot.services.PatreonService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class GuessGameHandler {
  private static GuessGameHandler gameHandler = null;

  private Map<Long, GuessGame> gameMap;

  private GuessGameHandler() {
    gameMap = new HashMap<>();
  }

  public static GuessGameHandler getInstance() {
    if (gameHandler == null) {
      gameHandler = new GuessGameHandler();
    }
    return gameHandler;
  }

  public void handleGuess(String guessWord, MessageReceivedEvent event) {
    long channelId = event.getChannel().getIdLong();
    long authorId = event.getAuthor().getIdLong();
    if (gameMap.get(channelId).guess(guessWord)) {
      if (PatreonService.getInstance().hasUserCustomCorrectReactions(authorId)) {
        try {
          event.getMessage().addReaction(PatreonService.getInstance().getCorrectReaction(authorId)).queue();
        } catch (Exception e) {
        }
      } else {
        event.getMessage().addReaction("U+1F389").queue();
      }
      gameMap.get(channelId).endGameAsWin(event);
      return;
    } else {
      if (PatreonService.getInstance().hasUserCustomWrongReactions(authorId)) {
        event.getMessage().addReaction(PatreonService.getInstance().getWrongReaction(authorId)).queue();
      }
    }
  }

  public void handlePlayCapitalCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    OptionMapping roundsOption = event.getOption("rounds");
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    GuessGame guessGame = new CapitalGuessGame(event.getChannel(), rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayFlagCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    OptionMapping difficultyOption = event.getOption("mode");
    OptionMapping roundsOption = event.getOption("rounds");
    OptionMapping continentOption = event.getOption("continent");
    String difficultyString = difficultyOption == null ? "sovereign countries only"
        : difficultyOption.getAsString().toLowerCase();
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
    String continentCode;
    if (continentOption == null) {
      continentCode = "all";
    } else {
      String continent = continentOption.getAsString().toLowerCase();
      if (GuessGameUtil.getInstance().isValidContinent(continent)) {
        continentCode = GuessGameUtil.getInstance().getContinentCode(continent);
        difficulty = 2;
      } else {
        continentCode = "all";
      }
    }
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    Optional<String> langOptional = LanguageService.getInstance()
        .getLanguageSelected(event.getGuild().getIdLong());
    GuessGame guessGame = new FlagGuessGame(event.getChannel(), difficulty, rounds, rounds, langOptional.orElse(null),
        continentCode, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayMapCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    OptionMapping difficultyOption = event.getOption("include_non_sovereign_countries");
    OptionMapping roundsOption = event.getOption("rounds");
    boolean isHard = difficultyOption == null ? false : difficultyOption.getAsBoolean();
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    Optional<String> langOptional = LanguageService.getInstance()
        .getLanguageSelected(event.getGuild().getIdLong());
    GuessGame guessGame = new MapGuessGame(event.getChannel(), isHard, rounds, rounds, langOptional.orElse(null),
        event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayLogoCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    OptionMapping roundsOption = event.getOption("rounds");
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    GuessGame guessGame = new LogoGuessGame(event.getChannel(), rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayPlaceCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    OptionMapping roundsOption = event.getOption("rounds");
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    GuessGame guessGame = new PlaceGuessGame(event.getChannel(), rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayStateFlagCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    OptionMapping countryOption = event.getOption("country");
    String countrySelection = countryOption.getAsString();
    String countryCode = GuessGameUtil.getInstance().getCountryCode(countrySelection.toLowerCase());
    OptionMapping roundsOption = event.getOption("rounds");
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    GuessGame guessGame = new StateFlagGuessGame(event.getChannel(), countryCode, rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayCapitalButton(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    int roundSize = Integer.parseInt(event.getComponentId().split("_")[1]);
    GuessGame guessGame = new CapitalGuessGame(event.getChannel(), roundSize, roundSize, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayFlagButton(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    String[] commandData = event.getComponentId().split("_");
    byte difficulty = Byte.parseByte(commandData[1]);
    int rounds = Integer.parseInt(commandData[2]);
    String continentCode = commandData[3];
    Optional<String> langOptional = LanguageService.getInstance()
        .getLanguageSelected(event.getGuild().getIdLong());
    GuessGame guessGame = new FlagGuessGame(event.getChannel(), difficulty, rounds, rounds,
        langOptional.orElse(null),
        continentCode, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayMapButton(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    String[] commandData = event.getComponentId().split("_");
    boolean isHard = commandData[1].equals("Hard");
    int rounds = Integer.parseInt(commandData[2]);
    Optional<String> langOptional = LanguageService.getInstance()
        .getLanguageSelected(event.getGuild().getIdLong());
    GuessGame guessGame = new MapGuessGame(event.getChannel(), isHard, rounds, rounds, langOptional.orElse(null),
        event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayLogoButton(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    String[] commandData = event.getComponentId().split("_");
    int rounds = Integer.parseInt(commandData[1]);
    GuessGame guessGame = new LogoGuessGame(event.getChannel(), rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayPlaceButton(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    String[] commandData = event.getComponentId().split("_");
    int rounds = Integer.parseInt(commandData[1]);
    GuessGame guessGame = new PlaceGuessGame(event.getChannel(), rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handlePlayStateFlagButton(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue(
          message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
      return;
    }
    String[] commandData = event.getComponentId().split("_");
    String countryCode = commandData[1];
    int rounds = Integer.parseInt(commandData[2]);
    GuessGame guessGame = new StateFlagGuessGame(event.getChannel(), countryCode, rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), guessGame);
    GameEndService.getInstance()
        .scheduleEndGame(new GuessGameEndRunnable(guessGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
  }

  public void handleSkipRequest(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (!gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("Game Has already ended!").queue();
      return;
    }
    event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
    GuessGame game = gameMap.get(event.getChannel().getIdLong());
    gameMap.remove(event.getChannel().getIdLong());
    game.endGameAsLose();
  }

  public boolean isActiveGame(long channelId) {
    return this.gameMap.containsKey(channelId);
  }

  public void removeGame(long channelId) {
    gameMap.remove(channelId);
  }

  void requestEndGame(GuessGame guessGame, long channelId) {
    if (gameMap.get(channelId).hashCode() == guessGame.hashCode()) {
      gameMap.remove(channelId);
      guessGame.endGameAsLose();
    }
  }

  public void addThisGame(long channelId, GuessGame game) {
    gameMap.put(channelId, game);
  }

}
