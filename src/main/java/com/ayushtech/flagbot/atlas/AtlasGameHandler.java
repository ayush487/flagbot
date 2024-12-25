package com.ayushtech.flagbot.atlas;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.atlas.classic.ClassicAtlasGame;
import com.ayushtech.flagbot.atlas.quick.QuickAtlasGame;
import com.ayushtech.flagbot.atlas.rapid.RapidAtlasGame;
import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.services.GameEndService;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class AtlasGameHandler {
  private static AtlasGameHandler atlasCommandHandler = null;

  private Map<Long, AtlasGame> atlasGameMap;
  private final int DEFAULT_MAX_ROUNDS = 10;
  private final int DEFAULT_ROUND_TIME = 15;
  private final int DEFAULT_MAX_SCORE = 30;
  private final int DEFAULT_BET_AMOUNT = 0;
  private final String ATLAS_GAME_IMAGE = "https://cdn.discordapp.com/attachments/1133277774010925206/1319749920852410420/globe_question.jpg?ex=67671864&is=6765c6e4&hm=8f7db66b8f9ba8f6f9962f261c6b55ce1815191068929a940604b07da9160d67&";

  private AtlasGameHandler() {
    atlasGameMap = new HashMap<>();
  }

  public static AtlasGameHandler getInstance() {
    if (atlasCommandHandler == null) {
      atlasCommandHandler = new AtlasGameHandler();
    }
    return atlasCommandHandler;
  }

  public void handleClassicMode(SlashCommandInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (atlasGameMap.containsKey(channelId)) {
      event.getHook().sendMessage("There is already a game running in current channel!").queue();
      return;
    }
    long userId = event.getUser().getIdLong();
    int roundsOption = event.getOption("rounds") == null ? DEFAULT_MAX_ROUNDS : event.getOption("rounds").getAsInt();
    int timeOption = event.getOption("time") == null ? DEFAULT_ROUND_TIME : event.getOption("time").getAsInt();
    int betAmountOption = event.getOption("bet_amount") == null ? DEFAULT_BET_AMOUNT
        : event.getOption("bet_amount").getAsInt();
    int maxScoreOption = event.getOption("max_score") == null ? DEFAULT_MAX_SCORE
        : event.getOption("max_score").getAsInt();
    int maxRound = DEFAULT_MAX_ROUNDS;
    int roundTime = DEFAULT_ROUND_TIME;
    int betAmount = DEFAULT_BET_AMOUNT;
    int maxScore = DEFAULT_MAX_SCORE;
    if (roundsOption < 2 || timeOption < 5 || maxScoreOption < 8 || betAmountOption < 0) {
      event.getHook().sendMessage("Invalid options!").queue();
      return;
    }
    if (roundsOption != DEFAULT_MAX_ROUNDS || timeOption != DEFAULT_ROUND_TIME || betAmountOption != DEFAULT_BET_AMOUNT
        || maxScoreOption != DEFAULT_MAX_SCORE) {
      if (!PatreonService.getInstance().isUserPatron(userId)) {
        event.getHook().sendMessage("You need to be a Patron to use the custom options!").queue();
        return;
      } else {
        maxRound = roundsOption > 25 ? 25 : roundsOption;
        maxScore = maxScoreOption > 100 ? 100 : maxScoreOption;
        roundTime = timeOption > 60 ? 60 : timeOption;
        betAmount = betAmountOption > 10_000 ? 10_000 : betAmountOption;
      }
    }
    if (betAmount > 0) {
      if (CoinDao.getInstance().getBalance(userId) < betAmount) {
        event.getHook().sendMessage("You don't have enough coins to bet this amount!")
            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        return;
      }
    }
    event.getHook().sendMessage("Starting a Atlas game in current channel!")
        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
    AtlasGame atlasGame = new ClassicAtlasGame(userId, event.getChannel(), maxRound, maxScore,
        roundTime, betAmount);
    atlasGameMap.put(channelId, atlasGame);
    GameEndService.getInstance().scheduleEndGame(() -> {
      if (atlasGameMap.get(channelId).hashCode() == atlasGame.hashCode()) {
        atlasGame.startGame();
      }
    }, 30, TimeUnit.SECONDS);
  }

  public void handleQuickMode(SlashCommandInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (atlasGameMap.containsKey(channelId)) {
      event.getHook().sendMessage("There is already a game running in current channel!").queue();
      return;
    }
    long userId = event.getUser().getIdLong();
    int roundsOption = event.getOption("rounds") == null ? DEFAULT_MAX_ROUNDS : event.getOption("rounds").getAsInt();
    int timeOption = event.getOption("time") == null ? DEFAULT_ROUND_TIME : event.getOption("time").getAsInt();
    int betAmountOption = event.getOption("bet_amount") == null ? DEFAULT_BET_AMOUNT
        : event.getOption("bet_amount").getAsInt();
    int maxScoreOption = event.getOption("max_score") == null ? DEFAULT_MAX_SCORE
        : event.getOption("max_score").getAsInt();
    int maxRound = DEFAULT_MAX_ROUNDS;
    int roundTime = DEFAULT_ROUND_TIME;
    int betAmount = DEFAULT_BET_AMOUNT;
    int maxScore = DEFAULT_MAX_SCORE;
    if (roundsOption < 2 || timeOption < 5 || maxScoreOption < 8 || betAmountOption < 0) {
      event.getHook().sendMessage("Invalid options!").queue();
      return;
    }
    if (roundsOption != DEFAULT_MAX_ROUNDS || timeOption != DEFAULT_ROUND_TIME || betAmountOption != DEFAULT_BET_AMOUNT
        || maxScoreOption != DEFAULT_MAX_SCORE) {
      if (!PatreonService.getInstance().isUserPatron(userId)) {
        event.getHook().sendMessage("You need to be a Patron to use the custom options!").queue();
        return;
      } else {
        maxRound = roundsOption > 25 ? 25 : roundsOption;
        maxScore = maxScoreOption > 100 ? 100 : maxScoreOption;
        roundTime = timeOption > 60 ? 60 : timeOption;
        betAmount = betAmountOption > 10_000 ? 10_000 : betAmountOption;
      }
    }
    if (!VotingService.getInstance().isUserVoted(userId) && !PatreonService.getInstance().isUserPatron(userId)) {
      event.getHook().sendMessage("You must vote for the bot in last 24 hours or be a Patron to use this mode!")
          .queue();
      return;
    }
    if (betAmount > 0) {
      if (CoinDao.getInstance().getBalance(userId) < betAmount) {
        event.getHook().sendMessage("You don't have enough coins to bet this amount!")
            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        return;
      }
    }
    event.getHook().sendMessage("Starting a Atlas game in current channel!")
        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
    AtlasGame atlasGame = new QuickAtlasGame(userId, event.getChannel(), maxRound, maxScore, roundTime, betAmount);
    atlasGameMap.put(channelId, atlasGame);
    GameEndService.getInstance().scheduleEndGame(() -> {
      if (atlasGameMap.get(channelId).hashCode() == atlasGame.hashCode()) {
        atlasGame.startGame();
      }
    }, 30, TimeUnit.SECONDS);
  }

  public void handleRapidMode(SlashCommandInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (atlasGameMap.containsKey(channelId)) {
      event.getHook().sendMessage("There is already a game running in current channel!").queue();
      return;
    }
    long userId = event.getUser().getIdLong();
    int roundsOption = event.getOption("rounds") == null ? DEFAULT_MAX_ROUNDS : event.getOption("rounds").getAsInt();
    int timeOption = event.getOption("time") == null ? DEFAULT_ROUND_TIME : event.getOption("time").getAsInt();
    int betAmountOption = event.getOption("bet_amount") == null ? DEFAULT_BET_AMOUNT
        : event.getOption("bet_amount").getAsInt();
    int maxScoreOption = event.getOption("max_score") == null ? DEFAULT_MAX_SCORE
        : event.getOption("max_score").getAsInt();
    int maxRound = DEFAULT_MAX_ROUNDS;
    int roundTime = DEFAULT_ROUND_TIME;
    int betAmount = DEFAULT_BET_AMOUNT;
    int maxScore = DEFAULT_MAX_SCORE;
    if (roundsOption < 2 || timeOption < 5 || maxScoreOption < 8 || betAmountOption < 0) {
      event.getHook().sendMessage("Invalid options!").queue();
      return;
    }
    if (roundsOption != DEFAULT_MAX_ROUNDS || timeOption != DEFAULT_ROUND_TIME || betAmountOption != DEFAULT_BET_AMOUNT
        || maxScoreOption != DEFAULT_MAX_SCORE) {
      if (!PatreonService.getInstance().isUserPatron(userId)) {
        event.getHook().sendMessage("You need to be a Patron to use the custom options!").queue();
        return;
      } else {
        maxRound = roundsOption > 25 ? 25 : roundsOption;
        maxScore = maxScoreOption > 100 ? 100 : maxScoreOption;
        roundTime = timeOption > 60 ? 60 : timeOption;
        betAmount = betAmountOption > 10_000 ? 10_000 : betAmountOption;
      }
    }
    if (!VotingService.getInstance().isUserVoted(userId) && !PatreonService.getInstance().isUserPatron(userId)) {
      event.getHook().sendMessage("You must vote for the bot in last 24 hours or be a Patron to use this mode!")
          .queue();
      return;
    }
    if (betAmount > 0) {
      if (CoinDao.getInstance().getBalance(userId) < betAmount) {
        event.getHook().sendMessage("You don't have enough coins to bet this amount!")
            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        return;
      }
    }
    event.getHook().sendMessage("Starting a Atlas game in current channel!")
        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
    AtlasGame atlasGame = new RapidAtlasGame(userId, event.getChannel(), maxRound, maxScore, roundTime, betAmount);
    atlasGameMap.put(channelId, atlasGame);
    GameEndService.getInstance().scheduleEndGame(() -> {
      if (atlasGameMap.get(channelId).hashCode() == atlasGame.hashCode()) {
        atlasGame.startGame();
      }
    }, 30, TimeUnit.SECONDS);
  }

  public void handleAtlasHelp(SlashCommandInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Atlas - Discover the World with Flag Bot!");
    eb.setThumbnail(ATLAS_GAME_IMAGE);
    eb.setColor(Color.PINK);
    eb.setDescription(
        "Embark on an epic geographical adventure with the **Atlas** command, an exciting multiplayer mode in Flag Bot!\nTest your knowledge of the world and compete with friends in various modes. 🌍🗺️");
    StringBuilder sb = new StringBuilder(
        "\n__Classic Mode__ : *Submit one answer per question. The first correct answer earns 5 points, the second 3 points, and the rest 1 point.*\n");
    sb.append("\n__Quick Mode__ : *Only the first correct answer wins, earning 5 points. Speed is key!*\n");
    sb.append(
        "\n__Rapid Mode__ : *Submit as many answers as you can. Points are awarded based on the number of correct answers given.*\n");
    sb.append(
        "\n__Note__ : `Quick and Rapid mode is only available for Patrons and users who voted for the bot in last 24 hours.`");
    eb.addField("__Modes__", sb.toString(), false);
    StringBuilder sb2 = new StringBuilder(
        "Options can be used to customize the game **(Restricted to Patrons only)**.\n");
    sb2.append(
        "`bet_amount` : *Set an entry fee for the game where the winner takes all. Default is 0 (max bet can be 10,000).*\n");
    sb2.append(
        "`rounds` : *Set the number of rounds the game will run. Default is 10 (customizable between 2 to 25).*\n");
    sb2.append(
        "`time` *Set the time provided each round to answer. Default is 15 seconds (modifiable between 5 and 60 seconds).*\n");
    sb2.append("`max_score` : *Set the score limit for the game. Default is 30 (customizable between 8 to 100).*\n");
    eb.addField("__Options__", sb2.toString(), false);
    eb.setFooter("f!exitatlas to quit the game midway",
        "https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }

  public void handleJoinButton(ButtonInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    String buttonId = event.getComponentId();
    if (!buttonId.equals("joinAtlasClassic")) {
      long userId = event.getUser().getIdLong();
      if (!VotingService.getInstance().isUserVoted(userId) && !PatreonService.getInstance().isUserPatron(userId)) {
        event.reply("You must vote for the bot in last 24 hours or be a Patron to join this game!")
            .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Vote"))
            .setEphemeral(true).queue();
        return;
      }
    }
    if (!atlasGameMap.containsKey(channelId)) {
      event.reply("No active game exist in this channel").setEphemeral(true).queue();
      return;
    }
    atlasGameMap.get(channelId).joinGame(event);
  }

  public void handleCancelStartButton(ButtonInteractionEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (!atlasGameMap.containsKey(channelId)) {
      event.reply("No active game exist in this channel").setEphemeral(true).queue();
      return;
    }
    boolean isCancelled = atlasGameMap.get(channelId).cancelStartGame(event, "You cancelled this game.");
    if (isCancelled) {
      atlasGameMap.remove(channelId);
    }
  }

  public void requestCancelGame(MessageReceivedEvent event) {
    long authorId = event.getAuthor().getIdLong();
    boolean isCancelled = atlasGameMap.get(event.getChannel().getIdLong()).cancelGame(authorId);
    if (isCancelled) {
      atlasGameMap.remove(event.getChannel().getIdLong());
    }
  }

  public boolean isGameExist(long channelId) {
    return atlasGameMap.containsKey(channelId);
  }

  public void handleAnswer(String messageText, MessageReceivedEvent event) {
    long authorId = event.getAuthor().getIdLong();
    long channelId = event.getChannel().getIdLong();
    if (atlasGameMap.get(channelId).isPlayerJoined(authorId)) {
      int atlasGameResponse = atlasGameMap.get(channelId).handleAnswer(authorId, messageText.toLowerCase());
      if (atlasGameResponse >= 0) {
        event.getMessage().addReaction(Emoji.fromUnicode(getReaction(atlasGameResponse))).queue();
      }
    }
  }

  public void removeGame(long channelId) {
    atlasGameMap.remove(channelId);
  }

  private String getReaction(int response) {
    if (response == 0)
      return "U+1F3C5";
    else if (response == 1)
      return "U+1F947";
    else if (response == 2)
      return "U+1F948";
    else
      return "U+1F949";
  }
}