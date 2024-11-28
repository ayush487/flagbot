package com.ayushtech.flagbot.guessGame.flag;

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
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class FlagGuessGame implements GuessGame {

  private MessageChannel channel;
  private Country country;
  private int rounds;
  private int roundSize;
  private byte difficulty;
  private long startTimeStamp;
  private long messageId;
  private MessageEmbed embed;
  private String lang;
  private String continentCode;
  private String continentName;

  public FlagGuessGame(MessageChannel channel, byte difficulty, int rounds, int roundSize, String lang,
      String continentCode,
      InteractionHook hook) {
    this.channel = channel;
    this.roundSize = roundSize;
    this.rounds = rounds;
    this.lang = lang;
    this.difficulty = difficulty;
    this.continentCode = continentCode;
    this.startTimeStamp = System.currentTimeMillis();
    String modeDisplayName;
    if (continentCode.equals("all")) {
      continentName = "Not Specified";
      if (difficulty == 0) {
        modeDisplayName = "Sovereign Countries Only";
        country = GuessGameUtil.getInstance().getRandomCountry(true);
      } else if (difficulty == 1) {
        modeDisplayName = "Non-Sovereign Countries Only";
        country = GuessGameUtil.getInstance().getRandomCountry(false);
      } else {
        modeDisplayName = "All Countries";
        country = GuessGameUtil.getInstance().getRandomCountry();
      }
    } else {
      modeDisplayName = "All Countries";
      continentName = GuessGameUtil.getInstance().getContinentName(continentCode);
      country = GuessGameUtil.getInstance().getRandomCountry(continentCode);
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Guess the Country Flag");
    eb.setColor(new Color(38, 187, 237));
    eb.setImage(country.getFlagImage());
    eb.setDescription(String.format("**Mode** : `%s`\n**Continent** : `%s`", modeDisplayName, continentName));
    eb.setFooter((difficulty != 0 ? "*Regions not available in the mode" : "*See Region will cost you 60 coins"));
    this.embed = eb.build();
    if (hook == null) {
      channel.sendMessageEmbeds(embed).setActionRow(Button.primary("skipGuess", "Skip"),
          difficulty == 0 ? Button.primary("checkRegionButton_" + country.getCode(), "See Region")
              : Button.primary("checkRegionButton", "See Region").asDisabled())
          .queue(message -> this.messageId = message.getIdLong());
    } else {
      hook.sendMessageEmbeds(embed).addActionRow(Button.primary("skipGuess", "Skip"),
          difficulty == 0 ? Button.primary("checkRegionButton_" + country.getCode(), "See Region")
              : Button.primary("checkRegionButton", "See Region").asDisabled())
          .queue(message -> this.messageId = message.getIdLong());
    }
  }

  @Override
  public void endGameAsWin(MessageReceivedEvent event) {
    GuessGameHandler.getInstance().removeGame(this.channel.getIdLong());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    String answerString;
    if (lang == null) {
      answerString = country.getName();
    } else {
      String altGuess = LanguageService.getInstance().getCorrectGuess(lang, country.getCode());
      answerString = String.format("%s (`%s`)", country.getName(), altGuess);
    }
    StringBuilder sb = new StringBuilder();
    sb.append(event.getAuthor().getAsMention() + " is correct!\n**Coins :** `"
        + CoinDao.getInstance().addCoinsAndGetBalance(event.getAuthor().getIdLong(), 100) + "(+100)` " + ":coin:"
        + "  \n**Correct Answer :** " + answerString);
    if (GuessGameUtil.getInstance().hasAlternativeName(country.getCode())) {
      sb.append("\n**Alternative Answers :** " + GuessGameUtil.getInstance().getAlternativeNames(country.getCode()));
    }
    sb.append("\n**Time Taken :** " + getTimeTook());
    eb.setDescription(sb.toString());
    eb.setThumbnail(country.getFlagImage());
    eb.setColor(new Color(13, 240, 52));
    if (rounds <= 1) {
      event.getChannel().sendMessageEmbeds(eb.build())
          .setActionRow(Button.primary("playAgainFlag_" + difficulty + "_" + roundSize + "_" + continentCode,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      event.getChannel().sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, difficulty, rounds - 1, roundSize, lang, continentCode);
    }
    disableButtons();
  }

  @Override
  public void endGameAsLose() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("No one guessed the flag!");
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
          .setActionRow(Button.primary("playAgainFlag_" + difficulty + "_" + roundSize + "_" + continentCode,
              roundSize <= 1 ? "Play Again" : "Start Round Again"))
          .queue();
    } else {
      this.channel.sendMessageEmbeds(eb.build()).queue();
      startAgain(channel, difficulty, rounds - 1, roundSize, lang, continentCode);
    }
    disableButtons();
  }

  @Override
  public void disableButtons() {
    this.channel.retrieveMessageById(this.messageId).complete().editMessageEmbeds(embed)
        .setActionRow(Button.primary("skipButton", "Skip").asDisabled(),
            Button.primary("checkRegion", "See Region").asDisabled())
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

  private static void startAgain(MessageChannel channel, byte difficulty, int rounds, int roundSize, String lang,
      String continentCode) {
    FlagGuessGame game = new FlagGuessGame(channel, difficulty, rounds, roundSize, lang, continentCode, null);
    GuessGameHandler.getInstance().addThisGame(channel.getIdLong(), game);
    GameEndService.getInstance().scheduleEndGame(new GuessGameEndRunnable(game, channel.getIdLong()), 30,
        TimeUnit.SECONDS);
  }

}