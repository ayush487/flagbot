package com.ayushtech.flagbot.atlas.classic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.ayushtech.flagbot.atlas.AtlasGameRound;
import com.ayushtech.flagbot.atlas.AtlasQuestion;
import com.ayushtech.flagbot.dbconnectivity.AtlasDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ClassicAtlasGameRound implements AtlasGameRound {
  private AtlasQuestion question;
  private List<Long> answeredBy;

  public ClassicAtlasGameRound(MessageChannel channel, int roundTime, int currentRound) {
    this.question = AtlasDao.getInstance().getQuestion();
    this.answeredBy = new ArrayList<>();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(question.getQuestion());
    eb.setThumbnail(thumbnailUrl);
    eb.setDescription("**Ends** <t:" + ((System.currentTimeMillis() / 1000) + roundTime) + ":R>\n__Mode__ : `CLASSIC`");
    eb.setColor(Color.YELLOW);
    eb.setFooter("Quickly type a answer  •  Round " + currentRound, flagbotURL);
    channel.sendMessageEmbeds(eb.build()).queue();
  }

  @Override
  public int handleAnswer(long userId, String answer) {
    if (answeredBy.contains(userId)) {
      return -1;
    }
    if (question.checkAnswer(answer)) {
      answeredBy.add(userId);
      return answeredBy.size();
    }
    return -1;
  }

  public List<Long> getWinnerList() {
    return this.answeredBy;
  }

}