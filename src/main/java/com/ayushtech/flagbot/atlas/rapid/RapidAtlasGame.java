package com.ayushtech.flagbot.atlas.rapid;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.atlas.AtlasGame;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class RapidAtlasGame extends AtlasGame {

  public RapidAtlasGame(long hostId, MessageChannelUnion channel, int maxRounds, int maxScore, int roundTime, int betAmount) {
    super(hostId, channel, "RAPID", maxRounds, maxScore, roundTime, betAmount);
    channel.sendMessageEmbeds(getStartGameEmbed())
        .setActionRow(Button.success("joinAtlasRapid", "Join"), Button.danger("cancelAtlas", "Cancel"))
        .queue(message -> setStartGameMessage(message.getIdLong()));
  }

  @Override
  protected void startNewRound() {
    this.currentRoundCount++;
    this.currentRound = new RapidAtlasGameRound(channel, roundTime, currentRoundCount);
    CompletableFuture.delayedExecutor(this.roundTime, TimeUnit.SECONDS).execute(() -> {
      this.endCurrentRound();
    });
  }

  @Override
  protected void endCurrentRound() {
    Map<Long,Integer> roundScores = ((RapidAtlasGameRound) this.currentRound).getRoundScoreMap();
    if (roundScores.size() == 0) {
      this.channel.sendMessage("No one got the correct answer!").queue();
    } else {
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Round End");
      eb.setColor(Color.WHITE);
      StringBuilder sb = new StringBuilder("__Top Players of this Round__:\n");
      roundScores.keySet().stream().sorted((a, b) -> roundScores.get(b) - roundScores.get(a)).forEach(id -> {
        increasePlayerScore(id, roundScores.get(id));
        sb.append("<@" + id + "> : " + roundScores.get(id) + "\n");
      });
      sb.append("\n__Current Standings__:\n");
      scoreMap.keySet().stream().sorted((a, b) -> scoreMap.get(b) - scoreMap.get(a)).forEach(id -> {
        sb.append("<@" + id + "> : " + scoreMap.get(id) + "\n");
      });
      eb.setDescription(sb.toString());
      this.channel.sendMessageEmbeds(eb.build()).queue();
    }
    inspectGame();
  }

}