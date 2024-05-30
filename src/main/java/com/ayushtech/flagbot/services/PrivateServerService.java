package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PrivateServerService {
  private static PrivateServerService privateServerService = null;
  private Map<Long, Long> slowdownMap;
  private long slowdown = 30000l;

  private PrivateServerService() {
    slowdownMap = new HashMap<>();
  }

  public static PrivateServerService getInstance() {
    if (privateServerService == null) {
      privateServerService = new PrivateServerService();
    }
    return privateServerService;
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
    eb.setDescription(
        "<:psychoworld:1244236059676774403> **Psychoworld** : [Psychoworld Android](https://drive.google.com/drive/folders/1L6PBo1jWARQBe5F3Nze_vf2bnl72ycew?usp=sharing) **|** [Psychoworld PC](https://drive.google.com/drive/folders/1rjCIO-UIx0sH-Gfi_mlGrpWrQbR2mIlf?usp=sharing)\n<:shinchan_game:1227575613792190589> **Shinchan** : [Shinchan Android](https://drive.google.com/drive/folders/1-nsh9Yrabauevgk9xXA5nBBAuxrl1GmL?usp=share_link) **|** [Shinchan PC](https://drive.google.com/drive/folders/1bbWi_qgRxuOm1BCNPZq72zpp0g36tGQN)\n<:Doraemon_Icon:1227847489869053982> **Doraemon** : [Doraemon Android](https://drive.google.com/drive/folders/12kfjAw6nsKgiW9X5RbgvdkPFAJEuCpiX?usp=sharing) **|** [Doraemon PC](https://drive.google.com/drive/folders/100N2Ym5Q1Ep8UNYB7NwX4OKt4rqvCdpC?usp=sharing)\n<:Ninja_Hattori:1227847481619124304> **Ninja Hattori** : [Ninja Hattori Android](https://drive.google.com/drive/folders/1nQ3Bs3xf3vjkxPEF9suG4s3wtP1n3XM4?usp=sharing) **|** [Ninja Hattori PC](https://drive.google.com/file/d/1LHctYejYVhb75ltBtkn6MaND-uJA9Vcz/view?usp=sharing)\n<:pyaari_nani:1227575628589568070> **Pyaari Nani** : [Pyaari Nani Android](https://play.google.com/store/apps/details?id=com.GamesOfVaibhav.PyariNani) **|** [Pyaari Nani PC](https://gamesofvaibhav.itch.io/pyari-nani-a-horror-robbery)\n<:skibidi:1227575620641361963> **Skibidi Toilet** : [Skibidi Android](https://drive.google.com/drive/folders/1MkL675w-obLRfJHuNoaa8-J5R-LgLp3d?usp=drive_link) **|** [Skibidi PC](https://drive.google.com/drive/folders/1YJPj_zu0533T4K5fZbvDQcoVBn9yHP-T)");
    eb.setFooter("Click on the links to download them", "https://cdn.discordapp.com/emojis/1227575632930668555.png");
    eb.addField("__Can't find your favourite game__ ?", "Check out these channels :\n<#895963312678895626>\n<#895963211101270046>",false);
    eb.addField("__How to download and install games__", "[Click here](https://www.youtube.com/watch?v=sPy45fv_L6A)", false);
    event.getMessage().replyEmbeds(eb.build()).queue(
        message -> message.delete().queueAfter(slowdown, TimeUnit.MILLISECONDS));
  }
}
