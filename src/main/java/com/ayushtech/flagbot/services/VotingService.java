package com.ayushtech.flagbot.services;

import java.time.LocalDateTime;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

public class VotingService {

  private static VotingService votingService = null;

  private final long vote_logs_channel = 1191691729825435669l;

  private VotingService() {
  }

  public static synchronized VotingService getInstance() {
    if (votingService == null) {
      return new VotingService();
    }
    return votingService;
  }

  public void voteUser(JDA jda, String voter_id) {
    String dmMsg;
    boolean isWeekend = isWeekend();
    if (isWeekend) {
      dmMsg = ":heart: **|** Thanks for voting us on [top.gg](https://top.gg/bot/1129789320165867662/vote)\n:money_with_wings: **|** You received 1000 :coin: as reward!\n:fireworks: **|** Since it's a weeakend, you receive 1000 :coin: extra!";
      CoinDao.getInstance().addCoins(Long.parseLong(voter_id), 2000l);
    } else {
      CoinDao.getInstance().addCoins(Long.parseLong(voter_id), 1000l);
      dmMsg = ":heart: **|** Thanks for voting us on [top.gg](https://top.gg/bot/1129789320165867662/vote)\n:money_with_wings: **|** You received 1000 :coin: as reward!";
    }
    jda.retrieveUserById(voter_id).queue(voter -> {
      voter.openPrivateChannel()
          .flatMap(channel -> channel.sendMessage(dmMsg))
          .queue();
      jda.getChannelById(MessageChannel.class, vote_logs_channel)
          .sendMessage("Rewards sent to User `" + voter.getName() + "`, id : `" + voter_id + "`").queue();
    });
  }

  private boolean isWeekend() {
    String day = LocalDateTime.now().minusMinutes(330l).getDayOfWeek().name();
    return (day.equals("SATURDAY") || day.equals("SUNDAY"));
  }

}
