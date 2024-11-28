package com.ayushtech.flagbot.race;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.game.fight.CountryOptions;
import com.ayushtech.flagbot.game.fight.FightUtils;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;
import com.ayushtech.flagbot.guessGame.logo.LogoOptions;
import com.ayushtech.flagbot.guessGame.logo.LogoUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class RaceHandler {
  private static RaceHandler raceHandler = null;

  private Map<Long, Race> raceMap;
  private ScheduledThreadPoolExecutor executor;
  private Random random;

  private RaceHandler() {
    raceMap = new HashMap<>();
    executor = new ScheduledThreadPoolExecutor(1);
    random = new Random();
  }

  public static RaceHandler getInstance() {
    if (raceHandler == null) {
      raceHandler = new RaceHandler();
    }
    return raceHandler;
  }

  public void handleRaceCommand(SlashCommandInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (raceMap.containsKey(channelId)) {
      event.reply("There is already a race going on in this channel\nTry in a different channel!").setEphemeral(true)
          .queue();
      return;
    }
    event.reply("Starting Race!").queue();
    Race race = new Race(event.getChannel(), event.getUser().getIdLong(),
        event.getUser().getName(), RaceType.valueOf(event.getSubcommandName().toUpperCase()));
    raceMap.put(event.getChannel().getIdLong(), race);
    executor.schedule(() -> {
      if (!race.isStarted() && raceMap.get(event.getChannel().getIdLong()).equals(race)) {
        race.endAsNotStarted();
        raceMap.remove(event.getChannel().getIdLong());
      }
    }, 120, TimeUnit.SECONDS);
    return;
  }

  public void handleStartRace(ButtonInteractionEvent event) {
    Race race = raceMap.get(event.getChannel().getIdLong()).startRace(event);
    if (race != null) {
      executor.schedule(() -> {
        if (raceMap.containsKey(event.getChannel().getIdLong())
            && raceMap.get(event.getChannel().getIdLong()).equals(race)) {
          race.endRaceAsTimeout();
          raceMap.remove(event.getChannel().getIdLong());
        }
      }, 300, TimeUnit.SECONDS);
    }
    return;
  }

  public void handleCancelRace(ButtonInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (raceMap.containsKey(channelId)) {
      if (raceMap.get(channelId).endAsCancelled(event))
        raceMap.remove(channelId);
    }
    return;
  }

  public void handleJoinRace(ButtonInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (raceMap.containsKey(channelId)) {
      raceMap.get(channelId).addParticipant(event);
    }
    return;
  }

  public void handleAccelerate(ButtonInteractionEvent event) {
    String type = event.getComponentId().split("_")[1];
    if (!raceMap.get(event.getChannel().getIdLong()).isRacerEntered(event.getUser().getIdLong())) {
      event.reply("You can't use this button").setEphemeral(true).queue();
      return;
    }
    RaceType raceType = RaceType.valueOf(type);
    switch (raceType) {
      case FLAGS:
        sendFlagOptions(event);
        break;
      case LOGO:
        sendLogoOptions(event);
        break;
      case MAPS:
        sendMapsOptions(event);
        break;
      case MATHS:
        sendMathsOptions(event);
        break;
      default:
        event.reply("Something went wrong").setEphemeral(true).queue();
        break;
    }
  }

  public void handleCorrectSelection(ButtonInteractionEvent event) {
    event.deferEdit().queue();
    if (raceMap.containsKey(event.getChannel().getIdLong())) {
      if (raceMap.get(event.getChannel().getIdLong()).moveDistance(event.getUser().getIdLong(),
          16 + random.nextInt(5))) {
        raceMap.remove(event.getChannel().getIdLong());
      }
    }
    event.getHook().deleteOriginal().queue();
  }

  public void handleWrongSelection(ButtonInteractionEvent event) {
    event.deferEdit().queue();
    if (raceMap.containsKey(event.getChannel().getIdLong())) {
      raceMap.get(event.getChannel().getIdLong()).moveDistance(event.getUser().getIdLong(), -15);
    }
    event.getHook().deleteOriginal().queue();
  }

  private void sendMathsOptions(ButtonInteractionEvent event) {
    MathOption option = MathUtils.getInstance().getMathOption();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Select the correct option");
    eb.setColor(Color.white);
    eb.setDescription("> Solve :\n### " + option.getExpression());
    event.replyEmbeds(eb.build())
        .addActionRow(
            Button.primary(
                option.isCorrectOption(0) ? "correct_" + 0 : "wrong_" + 0,
                option.getOption(0) + ""),
            Button.primary(
                option.isCorrectOption(1) ? "correct_" + 1 : "wrong_" + 1,
                option.getOption(1) + ""),
            Button.primary(
                option.isCorrectOption(2) ? "correct_" + 2 : "wrong_" + 2,
                option.getOption(2) + ""),
            Button.primary(
                option.isCorrectOption(3) ? "correct_" + 3 : "wrong_" + 3,
                option.getOption(3) + ""))
        .setEphemeral(true).queue();

  }

  private void sendMapsOptions(ButtonInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    CountryOptions options = FightUtils.getInstance().getOptions();
    eb.setTitle("Select the correct country");
    eb.setColor(Color.YELLOW);
    eb.setImage(GuessGameUtil.getInstance().getMapImage(options.getCorrectOption().getName()));
    event.replyEmbeds(eb.build())
        .addActionRow(
            Button.primary(
                options.getOptions()[0].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[0].getIsoCode()
                    : "wrong_" + options.getOptions()[0].getIsoCode(),
                options.getOptions()[0].getName()),
            Button.primary(
                options.getOptions()[1].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[1].getIsoCode()
                    : "wrong_" + options.getOptions()[1].getIsoCode(),
                options.getOptions()[1].getName()),
            Button.primary(
                options.getOptions()[2].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[2].getIsoCode()
                    : "wrong_" + options.getOptions()[2].getIsoCode(),
                options.getOptions()[2].getName()),
            Button.primary(
                options.getOptions()[3].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[3].getIsoCode()
                    : "wrong_" + options.getOptions()[3].getIsoCode(),
                options.getOptions()[3].getName()))
        .setEphemeral(true)
        .queue();
  }

  private void sendLogoOptions(ButtonInteractionEvent event) {
    LogoOptions options = LogoUtils.getInstance().getOptions();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Select the correct brand");
    eb.setColor(Color.LIGHT_GRAY);
    eb.setImage(String.format("https://raw.githubusercontent.com/ayush487/image-library/main/logo/%s.png",
        options.getCorrectOption()));
    event.replyEmbeds(eb.build())
        .addActionRow(
            Button.primary(options.isCorrectOption(0) ? "correct_" + options.getCodeOptions()[0]
                : "wrong_" + options.getCodeOptions()[0], options.getNameOptions()[0]),
            Button.primary(options.isCorrectOption(1) ? "correct_" + options.getCodeOptions()[1]
                : "wrong_" + options.getCodeOptions()[1], options.getNameOptions()[1]),
            Button.primary(options.isCorrectOption(2) ? "correct_" + options.getCodeOptions()[2]
                : "wrong_" + options.getCodeOptions()[2], options.getNameOptions()[2]),
            Button.primary(options.isCorrectOption(3) ? "correct_" + options.getCodeOptions()[3]
                : "wrong_" + options.getCodeOptions()[3], options.getNameOptions()[3]))
        .setEphemeral(true).queue();
  }

  private void sendFlagOptions(ButtonInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    CountryOptions options = FightUtils.getInstance().getOptions();
    eb.setTitle("Select the correct country");
    eb.setColor(Color.blue);
    eb.setImage("https://flagcdn.com/256x192/" + options.getCorrectOption().getIsoCode() + ".png");
    event.replyEmbeds(eb.build())
        .addActionRow(
            Button.primary(
                options.getOptions()[0].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[0].getIsoCode()
                    : "wrong_" + options.getOptions()[0].getIsoCode(),
                options.getOptions()[0].getName()),
            Button.primary(
                options.getOptions()[1].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[1].getIsoCode()
                    : "wrong_" + options.getOptions()[1].getIsoCode(),
                options.getOptions()[1].getName()),
            Button.primary(
                options.getOptions()[2].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[2].getIsoCode()
                    : "wrong_" + options.getOptions()[2].getIsoCode(),
                options.getOptions()[2].getName()),
            Button.primary(
                options.getOptions()[3].getIsoCode().equals(options.getCorrectOption().getIsoCode())
                    ? "correct_" + options.getOptions()[3].getIsoCode()
                    : "wrong_" + options.getOptions()[3].getIsoCode(),
                options.getOptions()[3].getName()))
        .setEphemeral(true)
        .queue();
  }
}
