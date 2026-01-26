package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.PollsDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class PrivateServerService {

  private static PrivateServerService privateServerService = null;
  private final Map<Long, Long> slowdownMap;
  private final long slowdown = 30000l;
  private final long ownerRole = 855371903116115979l;
  private final long adminRole = 896430596136525915l;
  private final long modRole = 855370894696382496l;
  private final long staffRole = 967323301628243979l;
  private final long pollChannel = 1255441568215597066l;
  private final long pollLogsChannel = 1115697363126857829l;
  private int adminThreshold;
  private int modThreshold;
  private int staffThreshold;
  private int totalVoteLimit;
  private final String embedContentUrl = "https://raw.githubusercontent.com/ayush487/flagbot/refs/heads/main/govembed.txt";
  private String embedDescription;

  private PrivateServerService() {
    slowdownMap = new HashMap<>();
    this.adminThreshold = 0;
    this.modThreshold = 0;
    this.staffThreshold = 0;
    this.totalVoteLimit = 0;
    embedDescription = getOnlineFileContent(embedContentUrl);
  }

  public static PrivateServerService getInstance() {
    if (privateServerService == null) {
      privateServerService = new PrivateServerService();
    }
    return privateServerService;
  }

  public void handlePollCommand(SlashCommandInteractionEvent event) {
    event.deferReply().setEphemeral(true).queue();
    Member member = event.getMember();
    boolean isMemberPermissible = member.getRoles().stream().map(role -> role.getIdLong())
        .anyMatch(roleId -> roleId == adminRole || roleId == ownerRole);
    if (!isMemberPermissible) {
      event.getHook().sendMessage("You are not allowed to use this command!").setEphemeral(true).queue();
      return;
    }
    int pollId = PollsDao.getInstance().getCurrentPollId();
    String pollText = event.getOption("text").getAsString();
    event.getHook().sendMessage("Sending poll in <#1255441568215597066>").setEphemeral(true).queue();
    PollsDao.getInstance().createPoll(pollId, pollText);
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("__Staff Decision__");
    eb.setDescription("**" + pollText + "**");
    eb.setFooter("Staff Polls #" + pollId);
    eb.setColor(Color.yellow);
    event.getJDA().getChannelById(MessageChannelUnion.class, pollChannel).sendMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("pollUpvote_" + pollId, "Upvote"),
                Button.danger("pollDownvote_" + pollId, "Downvote")),
            ActionRow.of(Button.secondary("pollViewVotes_" + pollId, "View Votes"),
                Button.primary("pollRemovevote_" + pollId, "Remove Your Vote")))
        .queue();
  }

  public void handlePollUpvote(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();
    String pollId = buttonId.split("_")[1];
    String userId = event.getUser().getId();
    boolean isUserVoted = PollsDao.getInstance().isUserVoted(pollId, userId);
    if (isUserVoted) {
      event.reply("You have already voted in this poll").setEphemeral(true).queue();
      return;
    }
    String memberRole = getMemberRole(event.getMember());
    if (memberRole == null) {
      event.reply("You can't vote").setEphemeral(true).queue();
      return;
    }
    int[] voteData = PollsDao.getInstance().upvotePoll(pollId, userId, memberRole);
    String pollMessageId = event.getMessageId();
    if (voteData[0] >= adminThreshold && voteData[2] >= modThreshold && voteData[4] >= staffThreshold) {
      event.editComponents(ActionRow.of(Button.success("approved", "Approved").asDisabled()),
          ActionRow.of(Button.secondary("pollViewVotes_" + pollId, "View Votes"))).queue();
      event.getHook().sendMessage("You upvoted this poll").setEphemeral(true).queue();
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Decision approved");
      eb.setDescription("[Click here to view poll](https://discord.com/channels/835384407368007721/1255441568215597066/"
          + pollMessageId + ")");
      eb.setColor(Color.green);
      event.getJDA().getChannelById(MessageChannelUnion.class, pollLogsChannel).sendMessageEmbeds(eb.build()).queue();
      return;
    } else if (voteData[0] + voteData[1] + voteData[2] + voteData[3] + voteData[4] + voteData[5] >= totalVoteLimit) {
      event.editComponents(ActionRow.of(Button.danger("disapproved", "Disapproved").asDisabled()),
          ActionRow.of(Button.secondary("pollViewVotes_" + pollId, "View Votes"))).queue();
      event.getHook().sendMessage("You upvoted this poll").setEphemeral(true).queue();
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Decision not approved");
      eb.setDescription("[Click here to view poll](https://discord.com/channels/835384407368007721/1255441568215597066/"
          + pollMessageId + ")");
      eb.setColor(Color.red);
      event.getJDA().getChannelById(MessageChannelUnion.class, pollLogsChannel).sendMessageEmbeds(eb.build()).queue();
      return;
    } else {
      event.reply("You upvoted this poll").setEphemeral(true).queue();
      return;
    }
  }

  public void handlePollDownvote(ButtonInteractionEvent event) {
    String buttonId = event.getComponentId();
    String pollId = buttonId.split("_")[1];
    String userId = event.getUser().getId();
    boolean isUserVoted = PollsDao.getInstance().isUserVoted(pollId, userId);
    if (isUserVoted) {
      event.reply("You have already voted in this poll").setEphemeral(true).queue();
      return;
    }
    String memberRole = getMemberRole(event.getMember());
    if (memberRole == null) {
      event.reply("You can't vote").setEphemeral(true).queue();
      return;
    }
    int[] voteData = PollsDao.getInstance().downvotePoll(pollId, userId, memberRole);
    if (voteData[0] + voteData[1] + voteData[2] + voteData[3] + voteData[4] + voteData[5] >= totalVoteLimit) {
      event.editComponents(ActionRow.of(Button.danger("disapproved", "Disapproved").asDisabled()),
          ActionRow.of(Button.secondary("pollViewVotes_" + pollId, "View Votes"))).queue();
      event.getHook().sendMessage("You downvoted this poll").setEphemeral(true).queue();
      return;
    } else {
      event.reply("You downvoted this poll").setEphemeral(true).queue();
      return;
    }
  }

  public void handleRemoveVote(ButtonInteractionEvent event) {
    String pollId = event.getComponentId().split("_")[1];
    String userId = event.getUser().getId();
    boolean isUserVoted = PollsDao.getInstance().isUserVoted(pollId, userId);
    if (!isUserVoted) {
      event.reply("You haven't voted yet.").setEphemeral(true).queue();
      return;
    }
    PollsDao.getInstance().removePollVote(pollId, userId);
    event.reply("You removed your vote\nYou can again vote on this poll").setEphemeral(true).queue();
    return;
  }

  public void handlePollViewVotes(ButtonInteractionEvent event) {
    event.deferReply().setEphemeral(true).queue();
    Member member = event.getMember();
    String pollId = event.getComponentId().split("_")[1];
    boolean isMemberPermissible = member.getRoles().stream().map(role -> role.getIdLong())
        .anyMatch(roleId -> roleId == adminRole || roleId == ownerRole);
    if (!isMemberPermissible) {
      event.getHook().sendMessage("You can't use this button").setEphemeral(true).queue();
      return;
    }
    StringBuilder sbAgree = new StringBuilder();
    StringBuilder sbDisagree = new StringBuilder();
    List<PollVoterData> voteList = PollsDao.getInstance().getPollVoterData(pollId);
    voteList.forEach(voter -> {
      if (voter.isAgree()) {
        sbAgree.append(String.format("<@%s> (`%s`)\n", voter.getUserId(), voter.getRole()));
      } else {
        sbDisagree.append(String.format("<@%s> (`%s`)\n", voter.getUserId(), voter.getRole()));
      }
    });
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Voters Data");
    eb.setColor(Color.BLUE);
    eb.addField("__Agree__", sbAgree.toString(), true);
    eb.addField("_Disagree_", sbDisagree.toString(), true);
    event.getHook().sendMessageEmbeds(eb.build()).setEphemeral(true).queue();
    return;
  }

  private String getMemberRole(Member member) {
    boolean isAdmin = member.getRoles().stream().map(Role::getIdLong)
        .anyMatch(roleId -> roleId == adminRole || roleId == ownerRole);
    if (isAdmin) {
      return "ADMIN";
    }
    boolean isMod = member.getRoles().stream().map(Role::getIdLong)
        .anyMatch(roleId -> roleId == modRole);
    if (isMod) {
      return "MODERATOR";
    }
    boolean isStaff = member.getRoles().stream().map(Role::getIdLong)
        .anyMatch(roleId -> roleId == staffRole);
    if (isStaff) {
      return "STAFF";
    }
    return null;
  }

  public void handleMessage(MessageReceivedEvent event) {
    long channelId = event.getChannel().getIdLong();
    if (!slowdownMap.containsKey(channelId)) {
      sendDownloadLinkMessage(event);
      slowdownMap.put(channelId, System.currentTimeMillis());
    } else {
      if (System.currentTimeMillis() - slowdownMap.get(channelId) >= slowdown) {
        sendDownloadLinkMessage(event);
        slowdownMap.put(channelId, System.currentTimeMillis());
      }
    }
  }

  private void sendDownloadLinkMessage(MessageReceivedEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("__Download Our Games__");
    eb.setColor(Color.GREEN);
    eb.setDescription(embedDescription);
    eb.setFooter("Click on the links to download them", "https://cdn.discordapp.com/emojis/1227575632930668555.png");
    eb.addField("__Can't find your favourite game__ ?",
        "Check out these channels :\n<#1465252474398183568>\n<#1465252368902783058>", false);
    eb.addField("__How to download and install games__", "[Click here](https://www.youtube.com/watch?v=sPy45fv_L6A)",
        false);
    event.getMessage().replyEmbeds(eb.build()).queue(
        message -> message.delete().queueAfter(slowdown, TimeUnit.MILLISECONDS));
  }

  public void setThreshold(int admin, int mod, int staff, int total) {
    this.adminThreshold = admin;
    this.modThreshold = mod;
    this.staffThreshold = staff;
    this.totalVoteLimit = total;
  }

  public void updateEmbedDescription() {
    this.embedDescription = getOnlineFileContent(embedContentUrl);
  }

  private String getOnlineFileContent(String fileUrl) {
    StringBuilder content = new StringBuilder();
    try {
      URL url = new URL(fileUrl);
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return content.toString();
  }

}
