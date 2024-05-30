package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.VoterDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

public class VotingService {

  private static VotingService votingService = null;
  private Map<Long, Long> voteData;
  private ScheduledThreadPoolExecutor executor;

  private final long vote_logs_channel = 1191691729825435669l;

  private VotingService() {
    voteData = VoterDao.getInstance().getRecentVoterData();
    executor = new ScheduledThreadPoolExecutor(1);
    executor.scheduleAtFixedRate(() -> {
      Set<Long> racentVotersSet = voteData.keySet().stream().filter(
          voterId -> voteData.get(voterId) + 86400000 > System.currentTimeMillis()).collect(Collectors.toSet());
      Map<Long, Long> newVoteData = new HashMap<>();
      racentVotersSet.stream().forEach(v -> newVoteData.put(v, voteData.get(v)));
      voteData = newVoteData;
    }, 30, 30, TimeUnit.MINUTES);
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
    voteData.put(Long.parseLong(voter_id), System.currentTimeMillis());
    jda.retrieveUserById(voter_id).queue(voter -> {
      voter.openPrivateChannel()
          .flatMap(channel -> channel.sendMessage(dmMsg))
          .queue();
      jda.getChannelById(MessageChannel.class, vote_logs_channel)
          .sendMessage("Rewards sent to User `" + voter.getName() + "`, id : `" + voter_id + "`").queue();
    });
    VoterDao.getInstance().addVoter(Long.parseLong(voter_id));
  }

  public void handleVoteInfoCommand(SlashCommandInteractionEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Recent Voters");
    eb.setColor(Color.GREEN);
    StringBuilder sb = new StringBuilder();
    voteData.keySet().forEach(
        id -> sb.append("<@" + id + "> " + TimeFormat.RELATIVE.atTimestamp(voteData.get(id)) + "\n"));
    eb.setDescription(sb.toString());
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }

  public boolean isUserVoted(long userId) {
    return voteData.containsKey(userId);
  }

  private boolean isWeekend() {
    String day = LocalDateTime.now().minusMinutes(330l).getDayOfWeek().name();
    return (day.equals("SATURDAY") || day.equals("SUNDAY"));
  }

}
