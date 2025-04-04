package com.ayushtech.flagbot.guessGame.state_flag;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.guessGame.GuessGame;
import com.ayushtech.flagbot.guessGame.GuessGameEndRunnable;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;
import com.ayushtech.flagbot.services.GameEndService;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class StateFlagGuessGame implements GuessGame {

  private MessageChannelUnion channel;
  private State state;
  private int rounds;
  private int roundSize;
  private long startTimeStamp;
  private long messageId;
  private MessageEmbed embed;
  private String countryCode;
  private String countryName;
  private boolean isSkippable;

  public StateFlagGuessGame(MessageChannelUnion channel, String countryCode, int rounds, int roundSize,
      boolean isSkippable,
      InteractionHook hook) {
    this.channel = channel;
    this.countryCode = countryCode;
    this.rounds = rounds;
    this.roundSize = roundSize;
    this.isSkippable = isSkippable;
    this.state = GuessGameUtil.getInstance().getRandomState(countryCode);
    this.startTimeStamp = System.currentTimeMillis();
    this.countryName = GuessGameUtil.getInstance().getCountryName(countryCode);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(String.format("Guess this %s of %s", GuessGameUtil.getInstance().getSubdivionTypeName(countryCode),
        countryName));
    eb.setImage(state.getFlag());
    eb.setColor(new Color(38, 187, 237));
    eb.setDescription(String.format("**Country** : `%s`", countryName));
    this.embed = eb.build();
    if (hook != null) {
      hook.sendMessage("Starting game now!").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
    }
    if (isSkippable) {
      channel.sendMessageEmbeds(this.embed).setActionRow(Button.primary("skipGuess", "Skip"))
          .queue(message -> this.messageId = message.getIdLong());
    } else {
      channel.sendMessageEmbeds(this.embed)
          .queue(message -> this.messageId = message.getIdLong());
    }
  }

  @Override
  public void endGameAsWin(MessageReceivedEvent event) {
    GuessGameHandler.getInstance().removeGame(this.channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    eb.setThumbnail(this.state.getFlag());
    eb.setColor(Color.green);
    StringBuilder sb = new StringBuilder();
    sb.append(event.getAuthor().getAsMention() + " is correct!\n");
    long userBalance = CoinDao.getInstance().addCoinsAndGetBalance(event.getAuthor().getIdLong(), 100);
    sb.append(String.format("**Coins** : `%d(+100)` :coin:\n", userBalance));
    sb.append(String.format("**%s** : `%s`\n", GuessGameUtil.getInstance().getSubdivionTypeName(countryCode),
        state.getName()));
    sb.append(String.format("**Country** : `%s`\n", countryName));
    sb.append(
        state.hasAlternativeName() ? String.format("**Alternative Name** : `%s`\n", state.getAlternativeName()) : "");
    sb.append(String.format("**Time Taken** : `%s`", getTimeTook()));
    eb.setDescription(sb.toString());
    if (rounds <= 1) {
      channel.sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainStateFlag_" + countryCode + "_" + roundSize + "_" + isSkippable,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      event.getChannel().sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, countryCode, rounds - 1, roundSize, isSkippable);
    }
    disableButtons();
  }

  @Override
  public void endGameAsLose() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the " + GuessGameUtil.getInstance().getSubdivionTypeName(countryCode).toLowerCase());
    eb.setThumbnail(state.getFlag());
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("**%s** : `%s`\n", GuessGameUtil.getInstance().getSubdivionTypeName(countryCode),
        state.getName()));
    sb.append(String.format("**Country** : `%s`\n", countryName));
    sb.append(
        state.hasAlternativeName() ? String.format("**Alternative Name** : `%s`", state.getAlternativeName()) : "");
    eb.setDescription(sb.toString());
    eb.setColor(Color.red);
    if (rounds <= 1) {
      channel.sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainStateFlag_" + countryCode + "_" + roundSize + "_" + isSkippable,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      channel.sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, countryCode, rounds - 1, roundSize, isSkippable);
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
    return this.state.getName().equalsIgnoreCase(guessString) || (this.state.getAlternativeName() == null
        ? false
        : this.state.getAlternativeName().equalsIgnoreCase(guessString));
  }

  private String getTimeTook() {
    long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
    String returnString = String.format("`%.1f seconds`", timeTookInMS / 1000.0);
    return returnString;
  }

  private static void startAgain(MessageChannelUnion channel, String countryCode, int rounds, int roundSize,
      boolean isSkippable) {
    GuessGame game = new StateFlagGuessGame(channel, countryCode, rounds, roundSize, isSkippable, null);
    GuessGameHandler.getInstance().addThisGame(channel.getIdLong(), game);
    GameEndService.getInstance().scheduleEndGame(new GuessGameEndRunnable(game, channel.getIdLong()), 30,
        TimeUnit.SECONDS);
  }

}
