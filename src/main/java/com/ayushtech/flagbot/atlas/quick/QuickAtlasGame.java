package com.ayushtech.flagbot.atlas.quick;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.atlas.AtlasGame;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class QuickAtlasGame extends AtlasGame {

  public QuickAtlasGame(long hostId, MessageChannelUnion channel, int maxRounds, int maxScore, int roundTime,
      int betAmount) {
    super(hostId, channel, "QUICK", maxRounds, maxScore, roundTime, betAmount);
    channel.sendMessageEmbeds(getStartGameEmbed())
        .setActionRow(Button.success("joinAtlasQuick", "Join"), Button.danger("cancelAtlas", "Cancel"))
        .queue(message -> setStartGameMessage(message.getIdLong()));
  }

  @Override
  protected void startNewRound() {
    this.currentRoundCount++;
    this.currentRound = new QuickAtlasGameRound(channel, roundTime, currentRoundCount);
    CompletableFuture.delayedExecutor(this.roundTime, TimeUnit.SECONDS).execute(() -> {
      this.endCurrentRound();
    });
  }

  @Override
  protected void endCurrentRound() {
    long winnerId = ((QuickAtlasGameRound) this.currentRound).getWinner();
    if (winnerId == 0) {
      this.channel.sendMessage("No one got the correct answer!").queue();
    } else {
      increasePlayerScore(winnerId, 5);
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Round End");
      eb.setColor(Color.WHITE);
      StringBuilder sb = new StringBuilder(String.format("<@%d> won this round! `(+5)`\n", winnerId));
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