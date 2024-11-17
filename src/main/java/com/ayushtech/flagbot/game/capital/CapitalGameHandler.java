package com.ayushtech.flagbot.game.capital;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.RegionDao;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.PatreonService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class CapitalGameHandler {

  private static CapitalGameHandler gameHandler = null;

  private HashMap<Long, CapitalGame> gameMap;
  private List<Capital> capitalList;
  private Random random;

  private CapitalGameHandler() {
    gameMap = new HashMap<>();
    capitalList = RegionDao.getInstance().getCapitalList();
    random = new Random();
  }

  public static synchronized CapitalGameHandler getInstance() {
    if (gameHandler == null) {
      gameHandler = new CapitalGameHandler();
    }
    return gameHandler;
  }

  public void handleGuess(String guessWord, MessageReceivedEvent event) {
    Long channelId = event.getChannel().getIdLong();
    Long authorId = event.getAuthor().getIdLong();
    if (gameMap.get(channelId).guess(guessWord)) {
      if (PatreonService.getInstance().hasUserCustomCorrectReactions(authorId)) {
        try {
          event.getMessage().addReaction(PatreonService.getInstance().getCorrectReaction(authorId)).queue();
        } catch (Exception e) {}
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

  public void handlePlayCommand(SlashCommandInteractionEvent event) {
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue();
      return;
    }
    OptionMapping roundsOption = event.getOption("rounds");
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    CapitalGame capitalGame = new CapitalGame(event.getChannel(), getRandomCapital(), rounds, rounds, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), capitalGame);
    GameEndService.getInstance()
        .scheduleEndGame(new CapitalGameEndRunnable(capitalGame, event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);

  }

  public void handlePlayCommand(ButtonInteractionEvent event) {
    event.deferReply().queue();
    if (gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("There is already a game running in this channel!").queue();
      return;
    }
    int roundSize = Integer.parseInt(event.getComponentId().split("_")[1]);
    CapitalGame capitalGame = new CapitalGame(event.getChannel(), getRandomCapital(), roundSize, roundSize, event.getHook());
    gameMap.put(event.getChannel().getIdLong(), capitalGame);
    GameEndService.getInstance()
        .scheduleEndGame(new CapitalGameEndRunnable(capitalGame, event.getChannel().getIdLong()), 15, TimeUnit.SECONDS);
  }

  public void handleSkipRequest(ButtonInteractionEvent event) {
    if (!gameMap.containsKey(event.getChannel().getIdLong())) {
      event.getHook().sendMessage("Game Has already ended!").queue();
      return;
    }
    event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
    CapitalGame game = gameMap.get(event.getChannel().getIdLong());
    gameMap.remove(event.getChannel().getIdLong());
    game.endGameAsLose();
  }

  public Capital getRandomCapital() {
    return capitalList.get(random.nextInt(capitalList.size()));
  }

  public Map<Long, CapitalGame> getGameMap() {
    return gameMap;
  }

}
