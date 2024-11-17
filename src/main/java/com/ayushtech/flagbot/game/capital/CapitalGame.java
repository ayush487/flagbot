package com.ayushtech.flagbot.game.capital;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.services.GameEndService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CapitalGame {

  private MessageChannel channel;
  private MessageEmbed messageEmbed;
  private Capital capital;
  private Long messageId;
  private int rounds;
  private int roundSize;
  private long startTimeStamp;

  private final String flagLink = "https://raw.githubusercontent.com/ayush487/image-library/main/flags/";
  private final String linkSuffix = ".png";

  public CapitalGame(MessageChannel channel, Capital capital, int rounds, int roundSize, InteractionHook hook) {
    this.channel = channel;
    this.capital = capital;
    this.rounds = rounds;
    this.roundSize = roundSize;
    this.startTimeStamp = System.currentTimeMillis();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess Capital of this Country");
    eb.setColor(new Color(38, 187, 237));
    eb.setThumbnail(flagLink + capital.getCountryCode() + linkSuffix);
    StringBuilder sb = new StringBuilder();
    sb.append("**Country** : `" + capital.getCountry() + "`");
    eb.setDescription(sb.toString());
    MessageEmbed embed = eb.build();
    setMessageEmbed(embed);
    if (hook == null) {
      channel.sendMessageEmbeds(embed).setActionRow(Button.primary("skipCapital", "Skip"))
          .queue(message -> setMessageId(message.getIdLong()));
    } else {
      hook.sendMessageEmbeds(embed).addActionRow(Button.primary("skipCapital", "Skip"))
          .queue(message -> setMessageId(message.getIdLong()));
    }
    return;
  }

  public void endGameAsWin(MessageReceivedEvent event) {
    CapitalGameHandler.getInstance().getGameMap().remove(channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    eb.setThumbnail(flagLink + capital.getCountryCode() + linkSuffix);
    eb.setColor(Color.green);
    StringBuilder sb = new StringBuilder();
    sb.append(event.getAuthor().getAsMention() + " is correct!\n");
    long userBalance = CoinDao.getInstance().addCoinsAndGetBalance(event.getAuthor().getIdLong(), 100);
    sb.append(String.format("**Coins** : `%d` :coin:\n", userBalance));
    sb.append(String.format("**Country** : %s\n**Capital** : `%s`\n", capital.getCountry(), capital.getCapital()));
    sb.append(String.format("**Time Taken** : `%s`", getTimeTook()));
    eb.setDescription(sb.toString());
    if (rounds <= 1) {
      event.getChannel().sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainCapital_" + roundSize,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      event.getChannel().sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, CapitalGameHandler.getInstance().getRandomCapital(), rounds-1, roundSize);
    }
    disableButtons();
  }

  public void endGameAsLose() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the capital");
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("**Country** : `%s`\n**Capital** : `%s`", capital.getCountry(), capital.getCapital()));
    eb.setThumbnail(flagLink + capital.getCountryCode() + linkSuffix);
    eb.setDescription(sb.toString());
    eb.setColor(Color.red);
    if (rounds <= 1) {
      channel.sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainCapital_" + roundSize,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      channel.sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, CapitalGameHandler.getInstance().getRandomCapital(), rounds-1, roundSize);
    }
    disableButtons();
  }

  public boolean guess(String guessCity) {
    guessCity = guessCity.toLowerCase();
    return guessCity.equals(capital.getCapital().toLowerCase());
  }

  private void disableButtons() {
    this.channel.retrieveMessageById(this.messageId).complete().editMessageEmbeds(getMessageEmbed())
        .setActionRow(Button.primary("skip", "Skip").asDisabled()).queue();
  }

  private void setMessageEmbed(MessageEmbed embed) {
    this.messageEmbed = embed;
  }

  private MessageEmbed getMessageEmbed() {
    return this.messageEmbed;
  }

  private String getTimeTook() {
    long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
    String returnString = String.format("`%.1f seconds`", timeTookInMS / 1000.0);
    return returnString;
  }

  private void setMessageId(long msgId) {
    this.messageId = msgId;
  }

  private static void startAgain(MessageChannel channel, Capital capital, int rounds, int roundSize) {
    CapitalGame cGame = new CapitalGame(channel, capital, rounds, roundSize, null);
    CapitalGameHandler.getInstance().getGameMap().put(channel.getIdLong(), cGame);
    GameEndService.getInstance().scheduleEndGame(new CapitalGameEndRunnable(cGame, channel.getIdLong()), 30,
        TimeUnit.SECONDS);
  }
}
