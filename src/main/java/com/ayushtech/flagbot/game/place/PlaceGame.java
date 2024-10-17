package com.ayushtech.flagbot.game.place;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.PlacesDao;
import com.ayushtech.flagbot.game.Game;
import com.ayushtech.flagbot.services.GameEndService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class PlaceGame {

  private static Random random = new Random();
  public static List<String> placeList;
  private MessageChannel channel;
  private Place place;
  private MessageEmbed messageEmbed;
  private long messageId;
  private int rounds;
  private int roundSize;
  private long startTimeStamp;

  static {
    PlaceGame.loadPlaceList();
  }

  public PlaceGame(MessageChannel channel, int rounds, int roundSize) {
    this.channel = channel;
    this.rounds = rounds;
    this.roundSize = roundSize;
    this.place = PlacesDao.getInstance().getPlace(getRandomPlace());
    this.startTimeStamp = System.currentTimeMillis();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess the Place");
    eb.setImage(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", place.getCode()));
    eb.setColor(new Color(83, 184, 224));
    this.messageEmbed = eb.build();
    this.channel.sendMessageEmbeds(messageEmbed)
        .setActionRow(Button.primary("skipPlace", "Skip"))
        .queue(msg -> setMessageId(msg.getIdLong()));
  }

  public void endGameAsWin(MessageReceivedEvent msgEvent) {
    PlaceGameHandler.getInstance().endGame(channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    StringBuilder sb = new StringBuilder(msgEvent.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
    + Game.getAmount(msgEvent.getAuthor().getIdLong()) + "(+100)` " + ":coin:"
    + "  \n **Correct Answer :** " + place.getName()
    + "\n**Location :** `" + place.getLocation() + "`");
    sb.append("\n**Time Taken :** " + getTimeTook());
    eb.setDescription(sb.toString());
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", place.getCode()));
    eb.setColor(new Color(13, 240, 52));
    if (rounds <= 1) {
      msgEvent.getChannel().sendMessageEmbeds(eb.build())
          .setActionRow(
              Button.primary("playAgainPlace_" + roundSize, roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      msgEvent.getChannel().sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, rounds - 1, roundSize);
    }
    disableButton();
    Game.increaseCoins(msgEvent.getAuthor().getIdLong(), 100l);
  }

  public void endGameAsLose() {
    PlaceGameHandler.getInstance().endGame(channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the place!");
    eb.setDescription("**Correct Answer :** \n" + place.getName() + "\n**Location : ** `" + place.getLocation() + "`");
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", place.getCode()));
    eb.setColor(new Color(240, 13, 52));
    if (rounds <= 1) {
      this.channel.sendMessageEmbeds(eb.build())
          .setActionRow(
              Button.primary("playAgainPlace_" + roundSize, roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      this.channel.sendMessageEmbeds(eb.build())
          .queue();
      startAgain(channel, rounds - 1, roundSize);
    }
    disableButton();
  }

  public static void startAgain(MessageChannel channel, int rounds, int roundSize) {
    PlaceGame game = new PlaceGame(channel, rounds, roundSize);
    PlaceGameHandler.getInstance().getGameMap().put(channel.getIdLong(), game);
    GameEndService.getInstance().scheduleEndGame(
        new PlaceGameEndRunnable(game, channel.getIdLong()), 30, TimeUnit.SECONDS);
  }

  public boolean guess(String guessWord) {
    return place.getName().equalsIgnoreCase(guessWord);
  }

  private void disableButton() {
    this.channel.retrieveMessageById(this.messageId).complete().editMessageEmbeds(this.messageEmbed)
        .setActionRow(Button.primary("skipPlace", "Skip").asDisabled())
        .queue();
  }

  private static void loadPlaceList() {
    placeList = PlacesDao.getInstance().getPlacesCodeList();
  }

  private String getRandomPlace() {
    return placeList.get(random.nextInt(placeList.size()));
  }

  private void setMessageId(long mId) {
    this.messageId = mId;
  }

  private String getTimeTook() {
    long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
    String returnString = String.format("`%.1f seconds`", timeTookInMS / 1000.0);
    return returnString;
  }
}