package com.ayushtech.flagbot.distanceGuess;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class GuessDistanceHandler {

  private static GuessDistanceHandler handler = null;

  private Map<Long, GuessDistance> distanceGameMap;

  private GuessDistanceHandler() {
    distanceGameMap = new HashMap<>();
  }

  public static GuessDistanceHandler getInstance() {
    if (handler == null) {
      handler = new GuessDistanceHandler();
    }
    return handler;
  }

  public void handleNewGameCommand(SlashCommandInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (distanceGameMap.containsKey(channelId)) {
      event.getHook().sendMessage("There is already a game running in this channel\nPlease try in a different channel!")
          .setEphemeral(true).queue();
      return;
    }
    long hostId = event.getUser().getIdLong();
    if (!VotingService.getInstance().isUserVoted(hostId)) {
      event.getHook().sendMessage("This command is only for users who have voted for us in last 24 hours!")
          .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Vote")).queue();
      return;
    }
    OptionMapping unitOption = event.getOption("unit");
    String unit = unitOption == null ? "kilometer" : unitOption.getAsString().toLowerCase();
    boolean isUnitKM = unit.startsWith("mile") ? false : true;
    event.getHook().sendMessage("Starting game in the current channel").queue();
    GuessDistance guessDistance = new GuessDistance(event.getChannel(), hostId, isUnitKM);
    distanceGameMap.put(channelId, guessDistance);
    GameEndService.getInstance().scheduleEndGame(() -> {
      if (!guessDistance.isStarted()) {
        distanceGameMap.remove(channelId);
        guessDistance.endGameAsTimeout();
      }
    }, 120, TimeUnit.SECONDS);
  }

  public void handleStartCommand(ButtonInteractionEvent event) {
    String hostId = event.getComponentId().split("_")[1];
    String userId = event.getUser().getId();
    if (!hostId.equals(userId)) {
      event.reply("Only host can start game").setEphemeral(true).queue();
      return;
    }
    long channelId = event.getChannel().getIdLong();
    if (distanceGameMap.containsKey(channelId)) {
      if (distanceGameMap.get(channelId).getJoinedPlayersCount() < 2) {
        event.reply("You need atleast 2 players to start this mode!").setEphemeral(true).queue();
        return;
      }
      distanceGameMap.get(channelId).startGame(event);
      event.getHook().sendMessage("Starting now!").queue();
    } else {
      event.reply("Something went wrong!").setEphemeral(true).queue();
    }
  }

  void removeGame(long channelId) {
    distanceGameMap.remove(channelId);
  }

  public void handleJoinCommand(ButtonInteractionEvent event) {
    long userId = event.getUser().getIdLong();
    if (!VotingService.getInstance().isUserVoted(userId)) {
      event.reply("You must vote for us to join or use this command")
          .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Vote"))
          .setEphemeral(true).queue();
      return;
    }
    long channelId = event.getChannel().getIdLong();
    if (!distanceGameMap.containsKey(channelId)) {
      event.reply("This game has already end or something went wrong").setEphemeral(true).queue();
    }
    distanceGameMap.get(channelId).addUser(userId, event);
    event.getHook().sendMessage("Joined!").setEphemeral(true).queue();
  }

  public void handleCancelCommand(ButtonInteractionEvent event) {
    String hostId = event.getComponentId().split("_")[1];
    String userId = event.getUser().getId();
    if (!hostId.equals(userId)) {
      event.reply("Only host can cancel game").setEphemeral(true).queue();
      return;
    }
    long channelId = event.getChannel().getIdLong();
    if (distanceGameMap.containsKey(channelId)) {
      distanceGameMap.get(channelId).cancelGame(event);
      distanceGameMap.remove(channelId);
    } else {
      event.reply("This game has already end or something went wrong").setEphemeral(true).queue();
      return;
    }
  }

  public void handleChangeUnitCommand(ButtonInteractionEvent event) {
    String hostId = event.getComponentId().split("_")[1];
    String userId = event.getUser().getId();
    if (!hostId.equals(userId)) {
      event.reply("Only host can change unit").setEphemeral(true).queue();
      return;
    }
    long channelId = event.getChannel().getIdLong();
    if (distanceGameMap.containsKey(channelId)) {
      distanceGameMap.get(channelId).changeDistanceUnit(event);
      event.getHook().sendMessage("Unit changed").setEphemeral(true).queue();
    } else {
      event.reply("This game has already end or something went wrong").setEphemeral(true).queue();
    }
  }

  public void handleGuess(String guess, MessageReceivedEvent event) {
    long channelId = event.getChannel().getIdLong();
    long userId = event.getAuthor().getIdLong();
    if (!distanceGameMap.containsKey(channelId) || !distanceGameMap.get(channelId).isStarted() || !distanceGameMap.get(channelId).isUserPlaying(userId)) {
      return;
    }
    if (isUptoSixDigitNumber(guess)) {
      distanceGameMap.get(channelId).addGuess(guess, userId, event);
      return;
    }
  }

  public boolean isActiveGameInChannel(long channelId) {
    return distanceGameMap.containsKey(channelId);
  }

  private boolean isUptoSixDigitNumber(String string) {
    return string.length() <= 6 && string.matches("\\d+");
  }
}