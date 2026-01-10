package com.ayushtech.flagbot.guessGame.map;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.guessGame.Country;
import com.ayushtech.flagbot.guessGame.GuessGame;
import com.ayushtech.flagbot.guessGame.GuessGameEndRunnable;
import com.ayushtech.flagbot.guessGame.GuessGameHandler;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.LanguageService;

import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class MapGuessGame implements GuessGame {

  private MessageChannelUnion channel;
  private Country country;
  private int rounds;
  private int roundSize;
  private boolean isHard;
  private long startTimeStamp;
  private long messageId;
  private String lang;
  private MessageEmbed embed;
  private boolean isSkippable;

  public MapGuessGame(MessageChannelUnion channel, boolean isHard, int rounds, int roundSize, boolean isSkippable,
      String lang,
      InteractionHook hook) {
    this.channel = channel;
    this.rounds = rounds;
    this.isHard = isHard;
    this.roundSize = roundSize;
    this.isSkippable = isSkippable;
    this.lang = lang;
    this.country = GuessGameUtil.getInstance().getRandomCountryForMapGuess(!isHard);
    this.startTimeStamp = System.currentTimeMillis();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess the country");
    eb.setImage(GuessGameUtil.getInstance().getMapImage(country.getCode()));
    eb.setColor(new Color(235, 206, 129));
    eb.setFooter("Map credit : utexas.edu");
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
    String answerString = lang == null ? country.getName()
        : String.format("%s (`%s`)", country.getName(),
            LanguageService.getInstance().getCorrectGuess(lang, country.getCode()));
    StringBuilder sb = new StringBuilder();
    sb.append(event.getAuthor().getAsMention() + " is correct!\n**Coins :** `" +
        CoinDao.getInstance().addCoinsAndGetBalance(event.getAuthor().getIdLong(), 100) +
        "(+100)` :coin:\n**Correct Answer :** " + answerString);
    if (GuessGameUtil.getInstance().hasAlternativeName(country.getCode())) {
      sb.append("\n**Alternative Answers :** " + GuessGameUtil.getInstance().getAlternativeNames(country.getCode()));
    }
    sb.append("\n**Time Taken :** " + getTimeTook());
    eb.setDescription(sb.toString());
    eb.setThumbnail(country.getFlagImage());
    eb.setColor(new Color(13, 240, 52));
    if (rounds <= 1) {
      event.getChannel().sendMessageEmbeds(eb.build())
          .setActionRow(
              Button.primary("playAgainMap_" + (isHard ? "Hard" : "Easy") + "_" + roundSize + "_" + isSkippable,
                  roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      event.getChannel().sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, isHard, rounds - 1, roundSize, isSkippable, lang);
    }
    disableButtons();
  }

  @Override
  public void endGameAsLose() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the map!");
    String answerString = lang == null ? country.getName()
        : String.format("%s (`%s`)", country.getName(),
            LanguageService.getInstance().getCorrectGuess(lang, country.getCode()));
    StringBuilder sb = new StringBuilder("**Correct Answer :** " + answerString);
    if (GuessGameUtil.getInstance().hasAlternativeName(country.getCode())) {
      sb.append("\n**Alternative Answers :** " + GuessGameUtil.getInstance().getAlternativeNames(country.getCode()));
    }
    eb.setDescription(sb.toString());
    eb.setThumbnail(country.getFlagImage());
    eb.setColor(new Color(240, 13, 52));
    if (rounds <= 1) {
      this.channel.sendMessageEmbeds(eb.build())
          .setActionRow(
              Button.primary("playAgainMap_" + (isHard ? "Hard" : "Easy") + "_" + roundSize + "_" + isSkippable,
                  roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      this.channel.sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, isHard, rounds - 1, roundSize, isSkippable, lang);
    }
    disableButtons();
  }

  @Override
  public void disableButtons() {
    this.channel.retrieveMessageById(messageId)
        .complete().editMessageEmbeds(embed)
        .setActionRow(Button.primary("skipButton", "Skip").asDisabled())
        .queue();
  }

  @Override
  public boolean guess(String guessString) {
    return country.getName().equalsIgnoreCase(guessString) ||
        LanguageService.getInstance().isGuessRight(lang, guessString, country.getCode());

  }

  private String getTimeTook() {
    long timeTookInMS = System.currentTimeMillis() - startTimeStamp;
    String returnString = String.format("`%.1f seconds`", timeTookInMS / 1000.0);
    return returnString;
  }

  private static void startAgain(MessageChannelUnion channel, boolean isHard, int rounds, int roundSize,
      boolean isSkippable, String lang) {
    GuessGame mapGame = new MapGuessGame(channel, isHard, rounds, roundSize, isSkippable, lang, null);
    GuessGameHandler.getInstance().addThisGame(channel.getIdLong(), mapGame);
    GameEndService.getInstance().scheduleEndGame(new GuessGameEndRunnable(mapGame, channel.getIdLong()), 30,
        TimeUnit.SECONDS);
  }

}
