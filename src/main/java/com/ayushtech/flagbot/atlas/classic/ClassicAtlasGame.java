package com.ayushtech.flagbot.atlas.classic;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.atlas.AtlasGame;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ClassicAtlasGame extends AtlasGame {

  public ClassicAtlasGame(long hostId, MessageChannel channel, int maxRounds, int maxScore, int roundTime,
      int betAmount) {
    super(hostId, channel, "CLASSIC", maxRounds, maxScore, roundTime, betAmount);
    channel.sendMessageEmbeds(getStartGameEmbed())
        .setActionRow(Button.success("joinAtlasClassic", "Join"), Button.danger("cancelAtlas", "Cancel"))
        .queue(message -> setStartGameMessage(message.getIdLong()));
  }

  @Override
  protected void startNewRound() {
    this.currentRoundCount++;
    this.currentRound = new ClassicAtlasGameRound(this.channel, roundTime, currentRoundCount);
    CompletableFuture.delayedExecutor(this.roundTime, TimeUnit.SECONDS).execute(() -> {
      this.endCurrentRound();
    });
  }

  @Override
  protected void endCurrentRound() {
    List<Long> roundWinners = ((ClassicAtlasGameRound) this.currentRound).getWinnerList();
    if (roundWinners.size() == 0) {
      this.channel.sendMessage("No one got the correct answer!").queue();
    } else {
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Round End");
      eb.setColor(Color.WHITE);
      StringBuilder sb = new StringBuilder("__Top Players of this Round__:\n");
      for (int i = 0; i < roundWinners.size(); i++) {
        increasePlayerScore(roundWinners.get(i), getScoreEarned(i + 1));
        sb.append(i + 1 + ". <@" + roundWinners.get(i) + "> `(+" + getScoreEarned(i + 1) + ")`\n");
      }
      sb.append("\n__Current Standings__:\n");
      scoreMap.keySet().stream().sorted((a, b) -> scoreMap.get(b) - scoreMap.get(a)).forEach(id -> {
        sb.append("<@" + id + "> : " + scoreMap.get(id) + "\n");
      });
      eb.setDescription(sb.toString());
      this.channel.sendMessageEmbeds(eb.build()).queue();
    }
    inspectGame();
  }

  private int getScoreEarned(int index) {
    if (index == 1)
      return 5;
    else if (index == 2)
      return 3;
    else
      return 1;
  }
}
