package com.ayushtech.flagbot.atlas;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class AtlasGame {
  private long hostId;
  private boolean isGameStarted;
  protected MessageChannel channel;
  protected Map<Long, Integer> scoreMap;
  private String mode;
  private String gameStartTimestamp;
  private long startgameMessageId;
  protected AtlasGameRound currentRound;
  protected int currentRoundCount = 0;
  protected final int maxRounds;
  protected final int maxScore;
  protected final int roundTime;
  protected final int betAmount;
  protected boolean isGameCancelled = false;
  protected final String atlasGameImageURL = "https://cdn.discordapp.com/attachments/1133277774010925206/1319749920852410420/globe_question.jpg?ex=67671864&is=6765c6e4&hm=8f7db66b8f9ba8f6f9962f261c6b55ce1815191068929a940604b07da9160d67&";
  protected final String thumbnailURL = "https://cdn.discordapp.com/attachments/1133277774010925206/1319633697279840287/robot_thinking.jpg?ex=6766ac27&is=67655aa7&hm=37cb0c0496cb40b04a6adca76072feef4b175bf3b78250ffd46e82c50207bdf9&";

  public AtlasGame(long hostId, MessageChannel channel, String mode, int maxRounds, int maxScore, int roundTime,
      int betAmount) {
    this.hostId = hostId;
    this.isGameStarted = false;
    this.channel = channel;
    this.mode = mode;
    this.maxRounds = maxRounds;
    this.maxScore = maxScore;
    this.roundTime = roundTime;
    this.betAmount = betAmount;
    this.scoreMap = new HashMap<>();
    this.scoreMap.put(hostId, 0);
    this.gameStartTimestamp = String.format("<t:%d:R>", (System.currentTimeMillis() / 1000) + 30);
  }

  protected abstract void startNewRound();

  protected abstract void endCurrentRound();

  protected void endGame() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Game Over");
    eb.setColor(Color.GREEN);
    eb.setThumbnail(atlasGameImageURL);
    StringBuilder sb = new StringBuilder();
    long winnerId = scoreMap.keySet().stream().sorted((a, b) -> scoreMap.get(b) - scoreMap.get(a)).findFirst()
        .orElse(0l);
    sb.append("<@" + winnerId + "> wins the game!\n");
    sb.append(betAmount > 0 ? "**Bet** : `" + betAmount + "` :coin: \n" : "");
    sb.append("\n__Final Standings__:\n");
    scoreMap.keySet().stream().sorted((a, b) -> scoreMap.get(b) - scoreMap.get(a)).forEach(id -> {
      sb.append("<@" + id + "> : " + scoreMap.get(id) + "\n");
    });
    eb.setDescription(sb.toString());
    this.channel.sendMessageEmbeds(eb.build()).queue();
    AtlasGameHandler.getInstance().removeGame(this.channel.getIdLong());
    if (betAmount > 0 && scoreMap.size() > 1) {
      long[] losers = scoreMap.keySet().stream().filter(id -> id != winnerId).mapToLong(Long::longValue).toArray();
      CoinDao.getInstance().transferCoinsFromMultipleUsers(losers, winnerId, betAmount);
    }
  }

  protected void inspectGame() {
    if (isGameCancelled)
      return;
    if (this.currentRoundCount >= maxRounds) {
      endGame();
      return;
    }
    boolean hasAnyoneTouchedMaxScore = scoreMap.keySet().stream().filter(user_id -> scoreMap.get(user_id) >= maxScore)
        .findAny().isPresent();
    if (hasAnyoneTouchedMaxScore) {
      endGame();
    } else {
      startNewRound();
    }
  }

  void startGame() {
    this.isGameStarted = true;
    this.channel.retrieveMessageById(startgameMessageId).queue(message -> {
      message.editMessage(scoreMap.keySet().stream().map(id -> "<@" + id + ">").collect(Collectors.joining(" ")))
          .setEmbeds(getGameStartedEmbed()).setActionRow(Button.success("g", "Game Started").asDisabled()).queue();
    });
    startNewRound();
  }

  void joinGame(ButtonInteractionEvent event) {
    long userId = event.getUser().getIdLong();
    long userBalance = CoinDao.getInstance().getBalance(userId);
    if (betAmount > 0 && userBalance < betAmount) {
      event.reply("You don't have enough coins to join the game!").setEphemeral(true).queue();
      return;
    }
    if (isGameStarted) {
      event.reply("Game has already started!").setEphemeral(true).queue();
      return;
    }
    if (scoreMap.containsKey(userId)) {
      event.reply("You have already joined!").setEphemeral(true).queue();
      return;
    }
    scoreMap.put(userId, 0);
    event.editMessageEmbeds(getStartGameEmbed()).queue();
    return;
  }

  protected int handleAnswer(long idLong, String messageText) {
    if (this.currentRound!=null) {
      return this.currentRound.handleAnswer(idLong, messageText);
    } else {
      return -1;
    }
  }

  boolean cancelStartGame(ButtonInteractionEvent event, String reason) {
    if (isGameStarted) {
      event.reply("Game has already started!").setEphemeral(true).queue();
      return false;
    }
    long userId = event.getUser().getIdLong();
    if (userId != hostId) {
      event.reply("Only host can cancel game").setEphemeral(true).queue();
      return false;
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Cancelled!");
    eb.setColor(Color.red);
    eb.setDescription("__**Mode**__ : `" + this.mode + "`");
    eb.setThumbnail(thumbnailURL);
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", scoreMap.keySet().stream().map(id -> "<@" + id + ">").collect(Collectors.joining("\n")),
        false);
    eb.setFooter(reason);
    event.editMessageEmbeds(eb.build())
        .setActionRow(Button.success("joinAtlas", "Join").asDisabled(),
            Button.danger("cancelAtlas", "Cancel").asDisabled())
        .queue();
    return true;
  }

  boolean cancelGame(long userId) {
    if (userId != hostId) {
      return false;
    }
    if (!isGameStarted) {
      return false;
    }
    this.isGameCancelled = true;
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.red);
    eb.setDescription("Game cancelled by host");
    channel.sendMessageEmbeds(eb.build()).queue();
    return true;
  }

  protected void increasePlayerScore(long userId, int points) {
    if (this.scoreMap.containsKey(userId)) {
      this.scoreMap.put(userId, this.scoreMap.get(userId) + points);
    } else {
      this.scoreMap.put(userId, points);
    }
  }

  boolean isGameStarted() {
    return this.isGameStarted;
  }

  protected MessageEmbed getStartGameEmbed() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Atlas");
    eb.setColor(Color.cyan);
    eb.setThumbnail(atlasGameImageURL);
    eb.setDescription(String.format("**Start ** %s", gameStartTimestamp));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", scoreMap.keySet().stream().map(id -> "<@" + id + ">").collect(Collectors.joining("\n")),
        false);
    StringBuilder sb = new StringBuilder();
    sb.append("**Mode** : `" + mode + "`\n");
    sb.append("**Max Rounds** : `" + maxRounds + "`\n");
    sb.append("**Max Score** : `" + maxScore + "`\n");
    sb.append("**Round Time** : `" + roundTime + "s`\n");
    sb.append("**Bet** : `" + betAmount + "` :coin: \n");
    eb.addField("__Game details__", sb.toString(), false);
    return eb.build();
  }

  protected MessageEmbed getGameStartedEmbed() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Atlas");
    eb.setColor(Color.green);
    eb.setThumbnail(atlasGameImageURL);
    eb.setDescription(String.format("__**Mode**__ : `%s`", mode));
    eb.addField("__Host__", "<@" + hostId + ">", false);
    eb.addField("__Players__", scoreMap.keySet().stream().map(id -> "<@" + id + ">").collect(Collectors.joining("\n")),
        false);
    return eb.build();
  }

  protected void setStartGameMessage(long startgameMessageId) {
    this.startgameMessageId = startgameMessageId;
  }

  protected boolean isPlayerJoined(long userId) {
    return scoreMap.containsKey(userId);
  }
}
