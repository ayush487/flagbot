package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    commandMetricMap = new HashMap<>(30);
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
    String commandDataIntoString = commandMetricMap.keySet().stream().map(
        key -> String.format("**%s** : `%d`", key, commandMetricMap.get(key).get())).collect(Collectors.joining("\n"));
    eb.setDescription(commandDataIntoString);
    eb.addField("Start Time",TimeFormat.RELATIVE.atTimestamp(startingInstance)+"", false);
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }

  public void registerCommandData(SlashCommandInteractionEvent event) {
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
          default:
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
            commandMetricMap.get("race_maths").incrementAndGet();
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
      case "leaderboards":
        commandMetricMap.get("leaderboards").incrementAndGet();
      case "vote":
        commandMetricMap.get("vote").incrementAndGet();
      case "help":
        commandMetricMap.get("help").incrementAndGet();
      case "give":
        commandMetricMap.get("give_coins").incrementAndGet();
      case "balance":
        commandMetricMap.get("balance").incrementAndGet();
      default:
        return;
    }
  }

  public void registerCommandData(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();
    if (buttonId.startsWith("playAgainFlag")) {
      commandMetricMap.get("play_flag").incrementAndGet();
    } else if (buttonId.startsWith("playAgainMap")) {
      commandMetricMap.get("play_map").incrementAndGet();
    } else if (buttonId.startsWith("playAgainLogo")) {
      commandMetricMap.get("play_logo").incrementAndGet();
    } else if (buttonId.startsWith("playAgainLogo")) {
      commandMetricMap.get("play_place").incrementAndGet();
    }
    return;
  }

  private void loadCommandMetricMap() {
    commandMetricMap.put("guess_flag", new AtomicLong());
    commandMetricMap.put("guess_map", new AtomicLong());
    commandMetricMap.put("guess_logo", new AtomicLong());
    commandMetricMap.put("guess_place", new AtomicLong());
    commandMetricMap.put("battle", new AtomicLong());
    commandMetricMap.put("race_flags", new AtomicLong());
    commandMetricMap.put("race_maps", new AtomicLong());
    commandMetricMap.put("race_logo", new AtomicLong());
    commandMetricMap.put("race_maths", new AtomicLong());
    commandMetricMap.put("memoflip_easy", new AtomicLong());
    commandMetricMap.put("memoflip_medium", new AtomicLong());
    commandMetricMap.put("memoflip_hard", new AtomicLong());
    commandMetricMap.put("memoflip_scores", new AtomicLong());
    commandMetricMap.put("language_set", new AtomicLong());
    commandMetricMap.put("language_info", new AtomicLong());
    commandMetricMap.put("language_remove", new AtomicLong());
    commandMetricMap.put("invite", new AtomicLong());
    commandMetricMap.put("leaderboards", new AtomicLong());
    commandMetricMap.put("balance", new AtomicLong());
    commandMetricMap.put("stocks_list", new AtomicLong());
    commandMetricMap.put("stocks_buy", new AtomicLong());
    commandMetricMap.put("stocks_sell", new AtomicLong());
    commandMetricMap.put("stocks_owned", new AtomicLong());
    commandMetricMap.put("vote", new AtomicLong());
    commandMetricMap.put("help", new AtomicLong());
    commandMetricMap.put("give_coins", new AtomicLong());
    commandMetricMap.put("play_flag", new AtomicLong());
    commandMetricMap.put("play_map", new AtomicLong());
    commandMetricMap.put("play_logo", new AtomicLong());
    commandMetricMap.put("play_place", new AtomicLong());
  }
}
