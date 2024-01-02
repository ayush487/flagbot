package com.ayushtech.flagbot.services;

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
    jda.retrieveUserById(voter_id).queue(voter -> {
      voter.openPrivateChannel()
          .flatMap(channel -> channel.sendMessage(
              ":heart: **|** Thanks for voting us on [top.gg](https://top.gg/bot/1129789320165867662/vote)\n:money_with_wings: **|** You received 1000 :coin: as reward!"))
          .queue();
      jda.getChannelById(MessageChannel.class, vote_logs_channel)
          .sendMessage("Rewards sent to User `" + voter.getName() + "`, id : `" + voter_id + "`").queue();
    });
    CoinDao.getInstance().addCoins(Long.parseLong(voter_id), 1000l);
  }

}
