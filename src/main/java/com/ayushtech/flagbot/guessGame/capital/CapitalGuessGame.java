package com.ayushtech.flagbot.guessGame.capital;

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

public class CapitalGuessGame implements GuessGame {

  private MessageChannelUnion channel;
  private Capital capital;
  private int rounds;
  private int roundSize;
  private long startTimeStamp;
  private long messageId;
  private MessageEmbed embed;
  private boolean isSkippable;

  public CapitalGuessGame(MessageChannelUnion channel, int rounds, int roundSize, boolean isSkippable,
      InteractionHook hook) {
    this.channel = channel;
    this.capital = GuessGameUtil.getInstance().getRandomCapital();
    this.roundSize = roundSize;
    this.rounds = rounds;
    this.isSkippable = isSkippable;
    this.startTimeStamp = System.currentTimeMillis();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess Capital of this Country");
    eb.setColor(new Color(38, 187, 237));
    eb.setThumbnail(this.capital.getFlagLink());
    StringBuilder sb = new StringBuilder();
    sb.append("**Country** : `" + capital.getCountry() + "`");
    eb.setDescription(sb.toString());
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
    eb.setThumbnail(capital.getFlagLink());
    eb.setColor(Color.green);
    StringBuilder sb = new StringBuilder();
    sb.append(event.getAuthor().getAsMention() + " is correct!\n");
    long userBalance = CoinDao.getInstance().addCoinsAndGetBalance(event.getAuthor().getIdLong(), 100);
    sb.append(String.format("**Coins** : `%d(+100)` <:flag_coin:1472232340523843767>\n", userBalance));
    sb.append(String.format("**Country** : %s\n**Capital** : `%s`\n", capital.getCountry(), capital.getCapital()));
    sb.append(String.format("**Time Taken** : `%s`", getTimeTook()));
    eb.setDescription(sb.toString());
    if (rounds <= 1) {
      event.getChannel().sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainCapital_" + roundSize + "_" + isSkippable,
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
    eb.setTitle("No one guessed the capital");
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("**Country** : `%s`\n**Capital** : `%s`", capital.getCountry(), capital.getCapital()));
    eb.setThumbnail(capital.getFlagLink());
    eb.setDescription(sb.toString());
    eb.setColor(Color.red);
    if (rounds <= 1) {
      channel.sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainCapital_" + roundSize + "_" + isSkippable,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      channel.sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, rounds - 1, roundSize, isSkippable);
    }
    disableButtons();
  }

  @Override
  public void disableButtons() {
    this.channel.retrieveMessageById(this.messageId).complete().editMessageEmbeds(embed)
        .setActionRow(Button.primary("skip", "Skip").asDisabled()).queue();
  }

  @Override
  public boolean guess(String guessString) {
    return guessString.equalsIgnoreCase(capital.getCapital());
  }

  private String getTimeTook() {
    long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
    String returnString = String.format("`%.1f seconds`", timeTookInMS / 1000.0);
    return returnString;
  }

  private static void startAgain(MessageChannelUnion channel, int rounds, int roundSize, boolean isSkippable) {
    CapitalGuessGame game = new CapitalGuessGame(channel, rounds, roundSize, isSkippable, null);
    GuessGameHandler.getInstance().addThisGame(channel.getIdLong(), game);
    GameEndService.getInstance().scheduleEndGame(new GuessGameEndRunnable(game, channel.getIdLong()), 30,
        TimeUnit.SECONDS);
  }
}
