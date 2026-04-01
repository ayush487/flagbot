package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.utils.PingRecord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PrivateServerService {

  private static PrivateServerService privateServerService = null;
  private final Map<Long, Long> slowdownMap;
  private final long slowdown = 30000l;
  private final long chatSummonId = 1467494594899087422l;
  private final long vcSummonId = 1467494758946701354l;
  private final long communityHeadRoleId = 1476553851372048465l;
  private final String embedContentUrl = "https://raw.githubusercontent.com/ayush487/flagbot/refs/heads/main/govembed.txt";
  private String embedDescription;

  private PrivateServerService() {
    slowdownMap = new HashMap<>();
    embedDescription = getOnlineFileContent(embedContentUrl);
  }

  public static PrivateServerService getInstance() {
    if (privateServerService == null) {
      privateServerService = new PrivateServerService();
    }
    return privateServerService;
  }

  public void handleChatPingCommand(SlashCommandInteractionEvent event) {
    event.deferReply(true).queue();
    Member member = event.getMember();
    boolean isAllowed = false;
    for (Role role : member.getRoles()) {
      if (role.getIdLong() == communityHeadRoleId) {
        isAllowed = true;
        break;
      }
    }
    if (!isAllowed) {
      if (member.hasPermission(Permission.ADMINISTRATOR))
        isAllowed = true;
      else {
        event.getHook().sendMessage("You don't have permission to use this command").queue();
        return;
      }
    }
    OptionMapping channelMapping = event.getOption("channel");
    GuildChannelUnion channelUnion = channelMapping.getAsChannel();
    if (!channelUnion.getType().equals(ChannelType.TEXT)) {
      event.getHook().sendMessage("Only Text Channel is allowed for Chat Ping Command").queue();
      return;
    }
    if (isAllowed) {
      if (PingRecord.getInstance().isChatPingAllowed()) {
        event.getHook().sendMessage("Pinging the chat...").queue();
        OptionMapping textMapping = event.getOption("message");
        String text = "";
        if (textMapping != null) {
          text = textMapping.getAsString();
        }

        channelUnion.asTextChannel().sendMessage(String.format("<@&%d> %s", chatSummonId, text)).queue();
      } else
        event.getHook().sendMessage("Chat pinging is in cooldown").queue();
    }
  }

  public void handleVcPingCommand(SlashCommandInteractionEvent event) {
    event.deferReply(true).queue();
    Member member = event.getMember();
    boolean isAllowed = false;
    for (Role role : member.getRoles()) {
      if (role.getIdLong() == communityHeadRoleId) {
        isAllowed = true;
        break;
      }
    }
    if (!isAllowed) {
      if (member.hasPermission(Permission.ADMINISTRATOR))
        isAllowed = true;
      else {
        event.getHook().sendMessage("You don't have permission to use this command").queue();
        return;
      }
    }
    OptionMapping channelMapping = event.getOption("channel");
    GuildChannelUnion channelUnion = channelMapping.getAsChannel();
    if (!channelUnion.getType().equals(ChannelType.TEXT) && !channelUnion.getType().equals(ChannelType.VOICE)) {
      event.getHook().sendMessage("Only Text or Voice Channel is allowed for VC Ping Command").queue();
      return;
    }
    if (isAllowed) {
      if (PingRecord.getInstance().isVcPingAllowed()) {
        event.getHook().sendMessage("Pinging the VC...").queue();
        OptionMapping textMapping = event.getOption("message");
        String text = "";
        if (textMapping != null) {
          text = textMapping.getAsString();
        }
        if (channelUnion.getType().equals(ChannelType.TEXT)) {
          channelUnion.asTextChannel().sendMessage(String.format("<@&%d> %s", vcSummonId, text)).queue();
        } else {
          channelUnion.asVoiceChannel().sendMessage(String.format("<@&%d> %s", vcSummonId, text)).queue();
        }
      } else
        event.getHook().sendMessage("VC pinging is in cooldown").queue();
    }
  }

  public void handleMessage(MessageReceivedEvent event) {
    long channelId = event.getChannel().getIdLong();
    String messageContent = event.getMessage().getContentDisplay();
    if (messageContent.contains("@gamesofvaibhav"))
      return;
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

  public void updateEmbedDescription() {
    this.embedDescription = getOnlineFileContent(embedContentUrl);
  }

  private String getOnlineFileContent(String fileUrl) {
    StringBuilder content = new StringBuilder();
    try {
      URL url = URI.create(fileUrl).toURL();
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
