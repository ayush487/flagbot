package com.ayushtech.flagbot.guessGame.logo;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.guessGame.GuessGame;
import com.ayushtech.flagbot.guessGame.GuessGameEndRunnable;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;
import com.ayushtech.flagbot.services.GameEndService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LogoGuessGame implements GuessGame {

  private MessageChannelUnion channel;
  private String brandCode;
  private String brandName;
  private MessageEmbed embed;
  private long messageId;
  private int rounds;
  private int roundSize;
  private long startTimeStamp;
  private boolean isSkippable;

  public LogoGuessGame(MessageChannelUnion channel, int rounds, int roundSize, boolean isSkippable,
      InteractionHook hook) {
    this.channel = channel;
    this.brandCode = GuessGameUtil.getInstance().getRandomBrandCode();
    this.brandName = GuessGameUtil.getInstance().getBrandName(brandCode);
    this.rounds = rounds;
    this.roundSize = roundSize;
    this.isSkippable = isSkippable;
    this.startTimeStamp = System.currentTimeMillis();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess the Brand");
    eb.setImage(String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png", brandCode));
    eb.setColor(new Color(235, 206, 129));
    embed = eb.build();
    if (hook != null) {
      hook.sendMessage("Starting game now!").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
    }
    if (isSkippable) {
      channel.sendMessageEmbeds(embed).setActionRow(Button.primary("skipGuess", "Skip"))
          .queue(message -> this.messageId = message.getIdLong());
    } else {
      channel.sendMessageEmbeds(embed)
          .queue(message -> this.messageId = message.getIdLong());
    }
    return;
  }

  @Override
  public void endGameAsWin(MessageReceivedEvent event) {
    GuessGameHandler.getInstance().removeGame(this.channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    long userBalance = CoinDao.getInstance().addCoinsAndGetBalance(event.getAuthor().getIdLong(), 100);
    StringBuilder sb = new StringBuilder(event.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
        + userBalance + "(+100)` " + "<:flag_coin:1472232340523843767>" + "  \n **Correct Answer :** " + brandName);
    sb.append("\n**Time Taken :** " + getTimeTook());
    eb.setDescription(sb.toString());
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png", brandCode));
    eb.setColor(new Color(13, 240, 52));
    if (rounds <= 1) {
      event.getChannel().sendMessageEmbeds(eb.build()).setActionRow(
          Button.primary("playAgainLogo_" + roundSize + "_" + isSkippable,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      event.getChannel().sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, rounds - 1, roundSize, isSkippable);
    }
    disableButtons();
  }

  @Override
  public void endGameAsLose() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the logo!");
    StringBuilder sb = new StringBuilder("**Correct Answer :** \n" + brandName);
    eb.setDescription(sb.toString());
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png", brandCode));
    eb.setColor(new Color(240, 13, 52));
    if (rounds <= 1) {
      this.channel.sendMessageEmbeds(eb.build())
          .setActionRow(
              Button.primary("playAgainLogo_" + roundSize + "_" + isSkippable,
                  roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      this.channel.sendMessageEmbeds(eb.build())
          .queue();
      startAgain(channel, rounds - 1, roundSize, isSkippable);
    }
    disableButtons();
  }

  @Override
  public void disableButtons() {
    this.channel.retrieveMessageById(this.messageId).complete().editMessageEmbeds(this.embed)
        .setActionRow(Button.primary("skipLogo", "Skip").asDisabled())
        .queue();
  }

  @Override
  public boolean guess(String guessString) {
    return brandName.equalsIgnoreCase(guessString);
  }

  private String getTimeTook() {
    long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
    String returnString = String.format("`%.1f seconds`", timeTookInMS / 1000.0);
    return returnString;
  }

  private static void startAgain(MessageChannelUnion channel, int rounds, int roundSize, boolean isSkippable) {
    GuessGame logoGame = new LogoGuessGame(channel, rounds, roundSize, isSkippable, null);
    GuessGameHandler.getInstance().addThisGame(channel.getIdLong(), logoGame);
    GameEndService.getInstance().scheduleEndGame(new GuessGameEndRunnable(logoGame, channel.getIdLong()), 30,
        TimeUnit.SECONDS);
  }
}
