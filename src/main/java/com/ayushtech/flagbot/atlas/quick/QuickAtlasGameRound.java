package com.ayushtech.flagbot.atlas.quick;

import java.awt.Color;

import com.ayushtech.flagbot.atlas.AtlasGameRound;
import com.ayushtech.flagbot.atlas.AtlasQuestion;
import com.ayushtech.flagbot.dbconnectivity.AtlasDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class QuickAtlasGameRound implements AtlasGameRound {
  private AtlasQuestion question;
  private long winnerId = 0l;

  public QuickAtlasGameRound(MessageChannel channel, int roundTime, int currentRound) {
    this.question = AtlasDao.getInstance().getQuestion();
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
    if (winnerId==0l && question.checkAnswer(answer)) {
      this.winnerId = userId;
      return 0;
    }
    return -1;
  }
  
  public long getWinner() {
    return this.winnerId;
  }
}
