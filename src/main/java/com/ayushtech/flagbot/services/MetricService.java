package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class MetricService {
  private static MetricService metricService = null;
  private Map<String, AtomicLong> commandMetricMap;
  private long startingInstance;

  private MetricService() {
    startingInstance = System.currentTimeMillis();
    commandMetricMap = new HashMap<>(37);
    loadCommandMetricMap();
  }

  public static MetricService getInstance() {
    if (metricService == null) {
      metricService = new MetricService();
    }
    return metricService;
  }

  public void handleMetricCommand(SlashCommandInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("FlagBot Metrics");
    eb.setColor(Color.yellow);
    eb.setDescription("**Start Time : **" + TimeFormat.RELATIVE.atTimestamp(startingInstance));
    eb.addField("__Guess commands__",
        String.format(
            "__Slash commands__\n> flag : %d\n> logo : %d\n> map : %d\n> continent : %d\n> place : %d\n> capital : %d\n> state_flag : %d\n> distance : %d\n> location : %d\n__Buttons__\n> flag : %d\n> logo : %d\n> map : %d\n> continent : %d\n> place : %d\n> capital : %d\n> state_flag : %d\n> location : %d",
            commandMetricMap.get("guess_flag").get(), commandMetricMap.get("guess_logo").get(),
            commandMetricMap.get("guess_map").get(), commandMetricMap.get("guess_continent").get(),
            commandMetricMap.get("guess_place").get(), commandMetricMap.get("guess_capital").get(),
            commandMetricMap.get("guess_stateflag").get(),
            commandMetricMap.get("guess_distance").get(), commandMetricMap.get("guess_location").get(),
            commandMetricMap.get("play_flag").get(), commandMetricMap.get("play_logo").get(),
            commandMetricMap.get("play_map").get(), commandMetricMap.get("play_continent").get(),
            commandMetricMap.get("play_place").get(), commandMetricMap.get("play_capital").get(),
            commandMetricMap.get("play_stateflag").get(), commandMetricMap.get("play_location").get()),
        true);
    eb.addField("__Race Commands__",
        String.format("> flag : %d\n> map : %d\n> logo : %d\n> maths : %d",
            commandMetricMap.get("race_flags").get(), commandMetricMap.get("race_maps").get(),
            commandMetricMap.get("race_logo").get(), commandMetricMap.get("race_maths").get()),
        true);
    eb.addField("__Memoflip__",
        String.format("> Easy : %d\n> Medium : %d\n> Hard : %d\n> Scores : %d",
            commandMetricMap.get("memoflip_easy").get(), commandMetricMap.get("memoflip_medium").get(),
            commandMetricMap.get("memoflip_hard").get(), commandMetricMap.get("memoflip_scores").get()),
        true);
    eb.addField("__Atlas__",
        String.format("> Classic : %d\n> Quick : %d\n> Rapid : %d\n> Help : %d",
            commandMetricMap.get("atlas_classic").get(),
            commandMetricMap.get("atlas_quick").get(), commandMetricMap.get("atlas_rapid").get(),
            commandMetricMap.get("atlas_help").get()),
        false);
    eb.addField("__Stocks__",
        String.format("> List : %d\n> Sell : %d\n> Owned : %d",
            commandMetricMap.get("stocks_list").get(),
            commandMetricMap.get("stocks_sell").get(), commandMetricMap.get("stocks_owned").get()),
        true);
    eb.addField("__Other Commands__",
        String.format(
            "Battle : %d\nInvite : %d\nLeaderboards : %d\nBalance : %d\nVote : %d\nHelp : %d\nGive Coins : %d\n__Language__\n> set : %d\n> info : %d\n> remove : %d",
            commandMetricMap.get("battle").get(), commandMetricMap.get("invite").get(),
            commandMetricMap.get("leaderboards").get(),
            commandMetricMap.get("balance").get(), commandMetricMap.get("vote").get(),
            commandMetricMap.get("help").get(),
            commandMetricMap.get("give_coins").get(), commandMetricMap.get("language_set").get(),
            commandMetricMap.get("language_info").get(),
            commandMetricMap.get("language_remove").get()),
        false);
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }

  public void registerCommandData(SlashCommandInteractionEvent event) {
    CompletableFuture.runAsync(() -> updateCommandData(event));
  }

  public void registerCommandData(ButtonInteractionEvent event) {
    CompletableFuture.runAsync(() -> updateCommandData(event));
  }

  private void updateCommandData(SlashCommandInteractionEvent event) {
    switch (event.getName()) {
      case "guess": {
        switch (event.getSubcommandName()) {
          case "flag":
            commandMetricMap.get("guess_flag").incrementAndGet();
            return;
          case "map":
            commandMetricMap.get("guess_map").incrementAndGet();
            return;
          case "logo":
            commandMetricMap.get("guess_logo").incrementAndGet();
            return;
          case "place":
            commandMetricMap.get("guess_place").incrementAndGet();
            return;
          case "capital":
            commandMetricMap.get("guess_capital").incrementAndGet();
            return;
          case "distance":
            commandMetricMap.get("guess_distance").incrementAndGet();
            return;
          case "location":
            commandMetricMap.get("guess_location").incrementAndGet();
            return;
          case "state_flag":
            commandMetricMap.get("guess_stateflag").incrementAndGet();
            return;
          default:
            commandMetricMap.get("guess_continent").incrementAndGet();
            return;
        }
      }
      case "memoflip": {
        switch (event.getSubcommandName()) {
          case "easy":
            commandMetricMap.get("memoflip_easy").incrementAndGet();
            return;
          case "medium":
            commandMetricMap.get("memoflip_medium").incrementAndGet();
            return;
          case "hard":
            commandMetricMap.get("memoflip_hard").incrementAndGet();
            return;
          default:
            commandMetricMap.get("memoflip_scores").incrementAndGet();
            return;
        }
      }
      case "battle":
        commandMetricMap.get("battle").incrementAndGet();
        return;
      case "race": {
        switch (event.getSubcommandName()) {
          case "flags":
            commandMetricMap.get("race_flags").incrementAndGet();
            return;
          case "maths":
            commandMetricMap.get("race_maths").incrementAndGet();
            return;
          case "logo":
            commandMetricMap.get("race_logo").incrementAndGet();
            return;
          default:
            commandMetricMap.get("race_maps").incrementAndGet();
            return;
        }
      }
      case "language": {
        switch (event.getSubcommandName()) {
          case "set":
            commandMetricMap.get("language_set").incrementAndGet();
            return;
          case "info":
            commandMetricMap.get("language_info").incrementAndGet();
            return;
          default:
            commandMetricMap.get("language_remove").incrementAndGet();
            return;
        }
      }
      case "stocks": {
        switch (event.getSubcommandName()) {
          case "list":
            commandMetricMap.get("stocks_list").incrementAndGet();
            return;
          case "sell":
            commandMetricMap.get("stocks_sell").incrementAndGet();
            return;
          case "buy":
            commandMetricMap.get("stocks_buy").incrementAndGet();
            return;
          default:
            commandMetricMap.get("stocks_owned").incrementAndGet();
            return;
        }
      }
      case "invite":
        commandMetricMap.get("invite").incrementAndGet();
        return;
      case "leaderboards":
        commandMetricMap.get("leaderboards").incrementAndGet();
        return;
      case "vote":
        commandMetricMap.get("vote").incrementAndGet();
        return;
      case "help":
        commandMetricMap.get("help").incrementAndGet();
        return;
      case "give":
        commandMetricMap.get("give_coins").incrementAndGet();
        return;
      case "balance":
        commandMetricMap.get("balance").incrementAndGet();
        return;
      case "atlas":
        switch (event.getSubcommandName()) {
          case "classic":
            commandMetricMap.get("atlas_classic").incrementAndGet();
            return;
          case "quick":
            commandMetricMap.get("atlas_quick").incrementAndGet();
            return;
          case "help":
            commandMetricMap.get("atlas_help").incrementAndGet();
            return;
          default:
            commandMetricMap.get("atlas_rapid").incrementAndGet();
            return;
        }
      default:
        return;
    }
  }

  private void updateCommandData(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();
    if (buttonId.startsWith("playAgainFlag")) {
      commandMetricMap.get("play_flag").incrementAndGet();
    } else if (buttonId.startsWith("playAgainMap")) {
      commandMetricMap.get("play_map").incrementAndGet();
    } else if (buttonId.startsWith("playAgainLogo")) {
      commandMetricMap.get("play_logo").incrementAndGet();
    } else if (buttonId.startsWith("playAgainPlace")) {
      commandMetricMap.get("play_place").incrementAndGet();
    } else if (buttonId.startsWith("playAgainContinent")) {
      commandMetricMap.get("play_continent").incrementAndGet();
    } else if (buttonId.startsWith("playAgainLocation")) {
      commandMetricMap.get("play_location").incrementAndGet();
    } else if (buttonId.startsWith("playAgainCapital")) {
      commandMetricMap.get("play_capital").incrementAndGet();
    } else if (buttonId.startsWith("playAgainStateFlag")) {
      commandMetricMap.get("play_stateflag").incrementAndGet();
    }
    return;
  }

  private void loadCommandMetricMap() {
    commandMetricMap.put("guess_flag", new AtomicLong());
    commandMetricMap.put("guess_map", new AtomicLong());
    commandMetricMap.put("guess_logo", new AtomicLong());
    commandMetricMap.put("guess_place", new AtomicLong());
    commandMetricMap.put("guess_distance", new AtomicLong());
    commandMetricMap.put("guess_continent", new AtomicLong());
    commandMetricMap.put("guess_location", new AtomicLong());
    commandMetricMap.put("guess_capital", new AtomicLong());
    commandMetricMap.put("guess_stateflag", new AtomicLong());
    commandMetricMap.put("play_flag", new AtomicLong());
    commandMetricMap.put("play_map", new AtomicLong());
    commandMetricMap.put("play_logo", new AtomicLong());
    commandMetricMap.put("play_place", new AtomicLong());
    commandMetricMap.put("play_continent", new AtomicLong());
    commandMetricMap.put("play_location", new AtomicLong());
    commandMetricMap.put("play_capital", new AtomicLong());
    commandMetricMap.put("play_stateflag", new AtomicLong());
    commandMetricMap.put("race_flags", new AtomicLong());
    commandMetricMap.put("race_maps", new AtomicLong());
    commandMetricMap.put("race_maths", new AtomicLong());
    commandMetricMap.put("race_logo", new AtomicLong());
    commandMetricMap.put("memoflip_easy", new AtomicLong());
    commandMetricMap.put("memoflip_medium", new AtomicLong());
    commandMetricMap.put("memoflip_hard", new AtomicLong());
    commandMetricMap.put("memoflip_scores", new AtomicLong());
    commandMetricMap.put("stocks_list", new AtomicLong());
    commandMetricMap.put("stocks_sell", new AtomicLong());
    commandMetricMap.put("stocks_owned", new AtomicLong());
    commandMetricMap.put("battle", new AtomicLong());
    commandMetricMap.put("invite", new AtomicLong());
    commandMetricMap.put("leaderboards", new AtomicLong());
    commandMetricMap.put("balance", new AtomicLong());
    commandMetricMap.put("vote", new AtomicLong());
    commandMetricMap.put("help", new AtomicLong());
    commandMetricMap.put("give_coins", new AtomicLong());
    commandMetricMap.put("language_set", new AtomicLong());
    commandMetricMap.put("language_info", new AtomicLong());
    commandMetricMap.put("language_remove", new AtomicLong());
    commandMetricMap.put("atlas_classic", new AtomicLong());
    commandMetricMap.put("atlas_quick", new AtomicLong());
    commandMetricMap.put("atlas_rapid", new AtomicLong());
    commandMetricMap.put("atlas_help", new AtomicLong());
  }
}
