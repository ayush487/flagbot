package com.ayushtech.flagbot.distanceGuess;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.DistanceDao;
import com.ayushtech.flagbot.services.GameEndService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class GuessDistance {

  private static Random random = new Random();

  private final int TARGET = 30;

  private long hostId;
  private Map<Long, Integer> userScores;
  private boolean isUnitKM;
  private long messageId;
  private int roundCount;
  private MessageChannelUnion channel;
  private boolean isStarted;
  private DistanceRound currentRound;

  public GuessDistance(MessageChannelUnion channel, long hostId, boolean isUnitKM, boolean isUserPatron) {
    this.channel = channel;
    this.hostId = hostId;
    this.isUnitKM = isUnitKM;
    this.userScores = new HashMap<>();
    this.userScores.put(hostId, 0);
    this.roundCount = 0;
    this.isStarted = false;
    this.currentRound = null;
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.cyan);
    eb.setTitle("Guess Distance");
    eb.setDescription(String.format("**UNIT** : `%s`", isUnitKM ? "Kilometers" : "Miles"));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", "<@" + hostId + ">", false);
    channel.sendMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("startDistance_" + hostId, "Start"), Button.primary("joinDistance_" + isUserPatron, "Join")),
            ActionRow.of(Button.danger("cancelDistance_" + hostId, "Cancel"),
                Button.primary("changeDistanceUnit_" + hostId, "Change Unit")))
        .queue(message -> setMessageId(message.getIdLong()));
  }

  public void startGame(ButtonInteractionEvent event) {
    this.isStarted = true;
    StringBuilder sb = new StringBuilder();
    userScores.keySet().forEach(userId -> sb.append("<@" + userId + ">\n"));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.green);
    eb.setTitle("Guess Distance");
    eb.setDescription(String.format("**UNIT** : `%s`", isUnitKM ? "Kilometers" : "Miles"));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", sb.toString(), false);
    eb.setFooter("Game started!");
    event.editMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("startDistance", "Start").asDisabled(),
                Button.primary("joinDistance", "Join").asDisabled()),
            ActionRow.of(Button.danger("cancelDistance", "Cancel").asDisabled(),
                Button.primary("changeDistanceUnit", "Change Unit").asDisabled()))
        .queue();
    sendNewRound();
  }

  private void sendNewRound() {
    if (roundCount >= 10 || isAnyUserReachedTarget()) {
      endGameNormally();
      return;
    }
    int[] mapData = getRandomMapData();
    this.currentRound = new DistanceRound(mapData[0], mapData[isUnitKM ? 1 : 2], mapData[3], userScores.size());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess the distance covered by red line");
    eb.setImage("https://raw.githubusercontent.com/ayush487/image-library/main/distance-maps/" + currentRound.getCode()
        + ".png");
    eb.setDescription(String.format("**Unit** : `%s`\n**Zoom Level** : `%d`", isUnitKM ? "Kilometers" : "Miles",
        currentRound.getZoom()));
    eb.setColor(Color.yellow);
    eb.setFooter("Please write only 1000 if your guess is 1000 " + (isUnitKM ? "kilometers" : "miles"));
    channel.sendMessageEmbeds(eb.build()).queue();
    GameEndService.getInstance().scheduleEndGame(() -> {
      this.endCurrentRound();
      GameEndService.getInstance().scheduleEndGame(() -> {
        this.sendNewRound();
      }, 2, TimeUnit.SECONDS);
    }, 15, TimeUnit.SECONDS);
  }

  private void endCurrentRound() {
    long[] top3 = this.currentRound.getTop3Players();
    int i = 5;
    for (long l : top3) {
      if (l != 0) {
        int intialScore = userScores.get(l);
        userScores.put(l, intialScore + i);
        i -= 2;
      }
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Round end");
    eb.setThumbnail(
        "https://raw.githubusercontent.com/ayush487/image-library/main/distance-maps/" + currentRound.getCode()
            + ".png");
    eb.setDescription("**Distance** : `" + currentRound.getDistance() + "`");
    eb.setColor(Color.gray);
    StringBuilder sb = new StringBuilder();
    for (long l : top3) {
      if (l != 0l) {
        sb.append(String.format("<@%d> : `%d`\n", l, currentRound.getGuess(l)));
      }
    }
    StringBuilder sb2 = new StringBuilder();
    List<Long> users = new ArrayList<>(userScores.keySet());
    users.sort((a, b) -> userScores.get(b) - userScores.get(a));
    users.forEach(u -> sb2.append("<@" + u + "> : `" + userScores.get(u) + "`\n"));
    eb.addField("__Top Guesses__:", sb.toString(), false);
    eb.addField("__Scores__ : ", sb2.toString(), false);
    this.currentRound = null;
    channel.sendMessageEmbeds(eb.build()).queue();
  }

  private void endGameNormally() {
    EmbedBuilder eb = new EmbedBuilder();
    List<Long> users = new ArrayList<>(userScores.keySet());
    users.sort((a, b) -> userScores.get(b) - userScores.get(a));
    StringBuilder sb = new StringBuilder("**__Scores__ :**\n");
    users.forEach(uId -> sb.append("<@" + uId + "> : `" + userScores.get(uId) + "`\n"));
    eb.setTitle("We have a winner!");
    eb.setDescription("**__Winner__** : " + "<@" + users.get(0) + ">\n" + sb.toString());
    eb.setColor(Color.green);
    channel.sendMessageEmbeds(eb.build()).queue();
    GuessDistanceHandler.getInstance().removeGame(channel.getIdLong());
  }

  private boolean isAnyUserReachedTarget() {
    return userScores.keySet().stream().anyMatch(userId -> userScores.get(userId) >= TARGET);
  }

  public void cancelGame(ButtonInteractionEvent event) {
    StringBuilder sb = new StringBuilder();
    userScores.keySet().forEach(userId -> sb.append("<@" + userId + ">\n"));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.red);
    eb.setTitle("Cancelled!");
    eb.setDescription(String.format("**UNIT** : `%s`", isUnitKM ? "Kilometers" : "Miles"));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", sb.toString(), false);
    event.editMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("startDistance", "Start").asDisabled(),
                Button.primary("joinDistance", "Join").asDisabled()),
            ActionRow.of(Button.danger("cancelDistance", "Cancel").asDisabled(),
                Button.primary("changeDistanceUnit", "Change Unit").asDisabled()))
        .queue();
  }

  public void addUser(long userId, ButtonInteractionEvent event) {
    this.userScores.put(userId, 0);
    StringBuilder sb = new StringBuilder();
    userScores.keySet().forEach(uId -> sb.append("<@" + uId + ">\n"));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.cyan);
    eb.setTitle("Guess Distance");
    eb.setDescription(String.format("**UNIT** : `%s`", isUnitKM ? "Kilometers" : "Miles"));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", sb.toString(), false);
    event.editMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("startDistance_" + hostId, "Start"), Button.primary("joinDistance", "Join")),
            ActionRow.of(Button.danger("cancelDistance_" + hostId, "Cancel"),
                Button.primary("changeDistanceUnit_" + hostId, "Change Unit")))
        .queue();
  }

  public void endGameAsTimeout() {
    StringBuilder sb = new StringBuilder();
    userScores.keySet().forEach(userId -> sb.append("<@" + userId + ">\n"));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.red);
    eb.setTitle("Timeout");
    eb.setDescription(String.format("**UNIT** : `%s`", isUnitKM ? "Kilometers" : "Miles"));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", sb.toString(), false);
    channel.editMessageEmbedsById(messageId, eb.build())
        .setComponents(
            ActionRow.of(Button.success("startDistance", "Start").asDisabled(),
                Button.primary("joinDistance", "Join").asDisabled()),
            ActionRow.of(Button.danger("cancelDistance", "Cancel").asDisabled(),
                Button.primary("changeDistanceUnit", "Change Unit").asDisabled()))
        .queue();
  }

  public void changeDistanceUnit(ButtonInteractionEvent event) {
    this.isUnitKM = !this.isUnitKM;
    StringBuilder sb = new StringBuilder();
    userScores.keySet().forEach(userId -> sb.append("<@" + userId + ">\n"));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.cyan);
    eb.setTitle("Guess Distance");
    eb.setDescription(String.format("**UNIT** : `%s`", isUnitKM ? "Kilometers" : "Miles"));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", sb.toString(), false);
    event.editMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("startDistance_" + hostId, "Start"), Button.primary("joinDistance", "Join")),
            ActionRow.of(Button.danger("cancelDistance_" + hostId, "Cancel"),
                Button.primary("changeDistanceUnit_" + hostId, "Change Unit")))
        .queue();
  }

  public void addGuess(String guess, long userId, MessageReceivedEvent event) {
    if (this.currentRound.isUserAlreadyGuessed(userId)) {
      return;
    }
    int guessedDistance = Integer.parseInt(guess);
    this.currentRound.addGuess(userId, guessedDistance);
    event.getMessage().addReaction(Emoji.fromUnicode("U+1F44D")).queue();
  }

  public int getJoinedPlayersCount() {
    return this.userScores.size();
  }

  private static int[] getRandomMapData() {
    int r = 1 + random.nextInt(1500);
    return DistanceDao.getInstance().getMapData(r);
  }

  public boolean isStarted() {
    return this.isStarted;
  }

  public boolean isUserPlaying(long userId) {
    return userScores.containsKey(userId) && this.currentRound != null;
  }

  private void setMessageId(long mId) {
    this.messageId = mId;
  }
}