package com.ayushtech.flagbot.atlas.rapid;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.ayushtech.flagbot.atlas.AtlasGameRound;
import com.ayushtech.flagbot.atlas.AtlasQuestion;
import com.ayushtech.flagbot.dbconnectivity.AtlasDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class RapidAtlasGameRound implements AtlasGameRound {
  private AtlasQuestion question;
  private Map<Long, Integer> roundScoreMap;

  public RapidAtlasGameRound(MessageChannel channel, int roundTime, int currentRound) {
    this.question = AtlasDao.getInstance().getQuestion();
    this.roundScoreMap = new HashMap<>();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(question.getQuestion());
    eb.setThumbnail(thumbnailUrl);
    eb.setDescription("**Ends** <t:" + ((System.currentTimeMillis() / 1000) + roundTime) + ":R>\n__Mode__ : `CLASSIC`");
    eb.setColor(Color.YELLOW);
    eb.setFooter("Quickly type as many answers as you can  •  Round " + currentRound, flagbotURL);
    channel.sendMessageEmbeds(eb.build()).queue();
  }

  @Override
  public int handleAnswer(long userId, String answer) {
    if (question.checkAnswer(answer)) {
      if (roundScoreMap.containsKey(userId)) {
        roundScoreMap.put(userId, roundScoreMap.get(userId) + 1);
      } else {
        roundScoreMap.put(userId, 1);
      }
      return 0;
    }
    return -1;
  }

  public Map<Long, Integer> getRoundScoreMap() {
    return this.roundScoreMap;
  }

}
