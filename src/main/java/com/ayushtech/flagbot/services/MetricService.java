package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.ayushtech.flagbot.dbconnectivity.ChannelDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class MetricService {
  private static MetricService metricService = null;
  private Map<String, AtomicLong> commandMetricMap;
  private long startingInstance;
  private Map<Long, Map<String, AtomicInteger>> commandBuffer;
  private Map<Long, Map<String, AtomicInteger>> gamblingStatsMap;

  private MetricService() {
    startingInstance = System.currentTimeMillis();
    commandMetricMap = new HashMap<>(38);
    commandBuffer = new ConcurrentHashMap<>();
    gamblingStatsMap = new HashMap<>();
    loadCommandMetricMap();
  }

  public void flush() {
    ChannelDao.getInstance().addCommandUsageData(commandBuffer);
    commandBuffer.clear();
    ChannelDao.getInstance().addGamblingStats(gamblingStatsMap);
    gamblingStatsMap.clear();
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
    eb.addField("__Atlas__",
        String.format("> Classic : %d\n> Quick : %d\n> Rapid : %d\n> Help : %d",
            commandMetricMap.get("atlas_classic").get(),
            commandMetricMap.get("atlas_quick").get(), commandMetricMap.get("atlas_rapid").get(),
            commandMetricMap.get("atlas_help").get()),
        false);
    eb.addField("__Crossword__",
        String.format("> Crossword slash : %d\n> Crossword button : %d\n> Crossduel : %d",
            commandMetricMap.get("crossword_slash").get(),
            commandMetricMap.get("crossword_button").get(),
            commandMetricMap.get("crossduel_slash").get()),
        true);
    eb.addField("__Gambling__",
        String.format(
            "**Slash**\nSlots : %d\nMines : %d\nCoinflip : %d\n**Prefix**\nSlots : %d\nMines : %d\nCoinflip : %d",
            commandMetricMap.get("slots_slash").get(),
            commandMetricMap.get("mines_slash").get(),
            commandMetricMap.get("coinflip_slash").get(),
            commandMetricMap.get("slots_prefix").get(),
            commandMetricMap.get("mines_prefix").get(),
            commandMetricMap.get("coinflip_prefix").get()),
        false);
    eb.addField("__Other Commands__",
        String.format(
            "Invite : %d\nLeaderboards : %d\nBalance : %d\nVote : %d\nHelp : %d\nGive Coins : %d\nPrefix : %d\n__Language__\n> set : %d\n> info : %d\n> remove : %d",
            commandMetricMap.get("invite").get(),
            commandMetricMap.get("leaderboards").get(),
            commandMetricMap.get("balance").get(),
            commandMetricMap.get("vote").get(),
            commandMetricMap.get("help").get(),
            commandMetricMap.get("give_coins").get(),
            commandMetricMap.get("prefix_command").get(),
            commandMetricMap.get("language_set").get(),
            commandMetricMap.get("language_info").get(),
            commandMetricMap.get("language_remove").get()),
        false);
    eb.addField("Prefix Commands Added Recently",
        String.format("Crossword %d\nBalance : %d\nLeaderboard : %d\nInvite : %d\nHelp : %d\nCrossduel : %d\nVote : %d",
            commandMetricMap.get("crossword_prefix").get(),
            commandMetricMap.get("balance_prefix").get(),
            commandMetricMap.get("leaderboard_prefix").get(),
            commandMetricMap.get("invite_prefix").get(),
            commandMetricMap.get("help_prefix").get(),
            commandMetricMap.get("crossduel_prefix").get(),
            commandMetricMap.get("vote_prefix").get()),
        false);
    eb.addField("Patreon Requests Sents", commandMetricMap.get("patreon_request").get() + "", false);
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }

  public void registerCommandData(SlashCommandInteractionEvent event) {
    CompletableFuture.runAsync(() -> updateCommandData(event));
  }

  public void registerCommandData(ButtonInteractionEvent event) {
    CompletableFuture.runAsync(() -> updateCommandData(event));
  }

  public void registerCommandData(String command, long userId) {
    CompletableFuture.runAsync(() -> updateCommandData(command, userId));
  }

  public void incrementCommandData(String commandName) {
    CompletableFuture.runAsync(() -> {
      AtomicLong metric = commandMetricMap.get(commandName);
      if (metric != null) {
        metric.incrementAndGet();
      }
    });
  }

  private void updateCommandData(SlashCommandInteractionEvent event) {
    switch (event.getName()) {
      case "guess": {
        switch (event.getSubcommandName()) {
          case "flag":
            commandMetricMap.get("guess_flag").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_flag");
            return;
          case "map":
            commandMetricMap.get("guess_map").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_map");
            return;
          case "logo":
            commandMetricMap.get("guess_logo").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_logo");
            return;
          case "place":
            commandMetricMap.get("guess_place").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_place");
            return;
          case "capital":
            commandMetricMap.get("guess_capital").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_capital");
            return;
          case "distance":
            commandMetricMap.get("guess_distance").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_distance");
            return;
          case "location":
            commandMetricMap.get("guess_location").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_location");
            return;
          case "state_flag":
            commandMetricMap.get("guess_stateflag").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_state_flag");
            return;
          default:
            commandMetricMap.get("guess_continent").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "guess_continent");
            return;
        }
      }
      case "race": {
        switch (event.getSubcommandName()) {
          case "flags":
            commandMetricMap.get("race_flags").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "race_flag");
            return;
          case "maths":
            commandMetricMap.get("race_maths").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "race_maths");
            return;
          case "logo":
            commandMetricMap.get("race_logo").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "race_logo");
            return;
          default:
            commandMetricMap.get("race_maps").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "race_map");
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
        recordUserCommandData(event.getUser().getIdLong(), "give_coins");
        return;
      case "balance":
        commandMetricMap.get("balance").incrementAndGet();
        recordUserCommandData(event.getUser().getIdLong(), "balance");
        return;
      case "crossword":
        commandMetricMap.get("crossword_slash").incrementAndGet();
        recordUserCommandData(event.getUser().getIdLong(), "crossword");
        return;
      case "crossduel":
        commandMetricMap.get("crossduel_slash").incrementAndGet();
        recordUserCommandData(event.getUser().getIdLong(), "crossduel");
        return;
      case "slots":
        commandMetricMap.get("slots_slash").incrementAndGet();
        recordUserCommandData(event.getUser().getIdLong(), "slots");
        return;
      case "mines":
        commandMetricMap.get("mines_slash").incrementAndGet();
        recordUserCommandData(event.getUser().getIdLong(), "mines");
        return;
      case "prefix":
        commandMetricMap.get("prefix_command").incrementAndGet();
        return;
      case "coinflip":
        commandMetricMap.get("coinflip_slash").incrementAndGet();
        recordUserCommandData(event.getUser().getIdLong(), "coinflip");
        return;
      case "atlas":
        switch (event.getSubcommandName()) {
          case "classic":
            commandMetricMap.get("atlas_classic").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "atlas_classic");
            return;
          case "quick":
            commandMetricMap.get("atlas_quick").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "atlas_quick");
            return;
          case "help":
            commandMetricMap.get("atlas_help").incrementAndGet();
            return;
          default:
            commandMetricMap.get("atlas_rapid").incrementAndGet();
            recordUserCommandData(event.getUser().getIdLong(), "atlas_rapid");
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
      recordUserCommandData(event.getUser().getIdLong(), "guess_flag");
    } else if (buttonId.startsWith("playAgainMap")) {
      commandMetricMap.get("play_map").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_map");
    } else if (buttonId.startsWith("playAgainLogo")) {
      commandMetricMap.get("play_logo").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_logo");
    } else if (buttonId.startsWith("playAgainPlace")) {
      commandMetricMap.get("play_place").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_place");
    } else if (buttonId.startsWith("playAgainContinent")) {
      commandMetricMap.get("play_continent").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_continent");
    } else if (buttonId.startsWith("playAgainLocation")) {
      commandMetricMap.get("play_location").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_location");
    } else if (buttonId.startsWith("playAgainCapital")) {
      commandMetricMap.get("play_capital").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_capital");
    } else if (buttonId.startsWith("playAgainStateFlag")) {
      commandMetricMap.get("play_stateflag").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "guess_state_flag");
    } else if (buttonId.startsWith("newCrossword")) {
      commandMetricMap.get("crossword_button").incrementAndGet();
      recordUserCommandData(event.getUser().getIdLong(), "crossword");
    }
    return;
  }

  private void updateCommandData(String command, long userId) {
    switch (command) {
      case "slots":
        commandMetricMap.get("slots_prefix").incrementAndGet();
        recordUserCommandData(userId, "slots");
        break;
      case "coinflip":
        commandMetricMap.get("coinflip_prefix").incrementAndGet();
        recordUserCommandData(userId, "coinflip");
        break;
      case "mines":
        commandMetricMap.get("mines_prefix").incrementAndGet();
        recordUserCommandData(userId, "mines");
        break;
      case "crossword":
        commandMetricMap.get("crossword_prefix").incrementAndGet();
        recordUserCommandData(userId, "crossword");
        break;
      case "balance":
        commandMetricMap.get("balance_prefix").incrementAndGet();
        recordUserCommandData(userId, "balance");
        break;
      case "leaderboard":
        commandMetricMap.get("leaderboard_prefix").incrementAndGet();
        break;
      case "invite":
        commandMetricMap.get("invite_prefix").incrementAndGet();
        break;
      case "help":
        commandMetricMap.get("help_prefix").incrementAndGet();
        break;
      case "crossduel":
        commandMetricMap.get("crossduel_prefix").incrementAndGet();
        recordUserCommandData(userId, "crossduel");
        break;
      case "vote":
        commandMetricMap.get("vote_prefix").incrementAndGet();
        break;
      default:
        break;
    }
  }

  public void recordUserCommandData(long userId, String commandName) {
    commandBuffer.computeIfAbsent(userId, key -> new HashMap<>())
        .computeIfAbsent(commandName, key -> new AtomicInteger())
        .incrementAndGet();
  }

  public void updateGambleStats(long userId, String cmdName, boolean isWin, int amount) {
    cmdName += isWin ? "_win" : "_lose";
    Map<String, AtomicInteger> userStats = gamblingStatsMap.computeIfAbsent(userId, k -> new HashMap<>());
    userStats.computeIfAbsent(cmdName + "_amount", k -> new AtomicInteger()).addAndGet(amount);
    userStats.computeIfAbsent(cmdName + "_count", k -> new AtomicInteger()).incrementAndGet();
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
    commandMetricMap.put("crossword_slash", new AtomicLong());
    commandMetricMap.put("crossword_button", new AtomicLong());
    commandMetricMap.put("crossduel_slash", new AtomicLong());
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
    commandMetricMap.put("patreon_request", new AtomicLong());
    commandMetricMap.put("slots_slash", new AtomicLong());
    commandMetricMap.put("mines_slash", new AtomicLong());
    commandMetricMap.put("coinflip_slash", new AtomicLong());
    commandMetricMap.put("slots_prefix", new AtomicLong());
    commandMetricMap.put("mines_prefix", new AtomicLong());
    commandMetricMap.put("coinflip_prefix", new AtomicLong());
    commandMetricMap.put("crossword_prefix", new AtomicLong());
    commandMetricMap.put("balance_prefix", new AtomicLong());
    commandMetricMap.put("leaderboard_prefix", new AtomicLong());
    commandMetricMap.put("invite_prefix", new AtomicLong());
    commandMetricMap.put("help_prefix", new AtomicLong());
    commandMetricMap.put("crossduel_prefix", new AtomicLong());
    commandMetricMap.put("vote_prefix", new AtomicLong());
    commandMetricMap.put("prefix_command", new AtomicLong());
  }
}
