package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CaptchaService {

  private static CaptchaService captchaService = null;
  private HashMap<Long, String> captcheMap;
  private HashMap<Long, Integer> userWarnsMap;
  private Set<Long> blockedUsers;
  private Random random;
  private ScheduledThreadPoolExecutor userUnblockService;

  private final long banLogChannelId = 1193603977821028402l;

  private final Font[] fonts = {
      new Font("Eras Light ITC", Font.PLAIN, 40),
      new Font("Californian FB", Font.PLAIN, 40),
      new Font("Candara", Font.PLAIN, 40),
      new Font("Corbel Light", Font.PLAIN, 40),
      new Font("Lucida Fax", Font.PLAIN, 40)

  };
  private final char[] charArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
      'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

  private CaptchaService() {
    this.random = new Random();
    this.captcheMap = new HashMap<>();
    this.blockedUsers = new HashSet<>();
    this.userWarnsMap = new HashMap<>();
    userUnblockService = new ScheduledThreadPoolExecutor(5);
  }

  public static synchronized CaptchaService getInstance() {
    if (captchaService == null) {
      captchaService = new CaptchaService();
      return captchaService;
    }
    return captchaService;
  }

  public void sendCaptcha(SlashCommandInteractionEvent event) {
    String captchText = createCaptchaText();
    captcheMap.put(event.getUser().getIdLong(), captchText);
    userWarnsMap.put(event.getUser().getIdLong(), 0);
    File captcheImage = createImage(captchText, event.getUser().getId());
    event.getHook().sendMessage(":warning: |" + event.getUser().getAsMention()
        + ", Please DM me with only the following 6 letter word to check that you are a human!\n :black_medium_square: | If you have trouble solving the captcha, please ask us in our support guild!")
        .addFile(captcheImage).queue();
    event.getUser().openPrivateChannel()
        .flatMap(channel -> channel.sendMessage("Solve the captcha").addFile(captcheImage)).queue();
  }

  public void sendCaptcha(ButtonInteractionEvent event) {
    String captchText = createCaptchaText();
    captcheMap.put(event.getUser().getIdLong(), captchText);
    userWarnsMap.put(event.getUser().getIdLong(), 0);
    File captcheImage = createImage(captchText, event.getUser().getId());
    event.getHook().sendMessage(":warning: |" + event.getUser().getAsMention()
        + ", Please DM me with only the following 6 letter word to check that you are a human!\n :black_medium_square: | If you have trouble solving the captcha, please ask us in our support guild!")
        .addFile(captcheImage).queue();
    event.getUser().openPrivateChannel()
        .flatMap(channel -> channel.sendMessage("Solve the captcha").addFile(captcheImage)).queue();
  }

  public void blockUser(long userId) {
    blockedUsers.add(userId);
    userUnblockService.schedule(() -> {
      CaptchaService.getInstance().removeBlock(userId);
    }, 60, TimeUnit.MINUTES);
  }

  public void removeBlock(long userId) {
    blockedUsers.remove(userId);
  }

  public boolean userIsBlocked(long userId) {
    return blockedUsers.contains(userId);
  }

  public boolean userHasCaptched(long userId) {
    return captcheMap.containsKey(userId);
  }

  public void handleCaptchaAnswer(MessageReceivedEvent event, String answer) {
    long userId = event.getAuthor().getIdLong();
    String captchaText = captcheMap.get(userId);
    if (captchaText.equalsIgnoreCase(answer) || captchaText == null) {
      captcheMap.remove(userId);
      userWarnsMap.remove(userId);
      removeFile(userId);
      event.getChannel().sendMessage(":thumbsup: **|** You are verified. Thank you!").queue();

    } else {
      int warnCount = userWarnsMap.get(userId);
      if (warnCount < 6) {
        userWarnsMap.put(userId, warnCount + 1);
        event.getChannel()
            .sendMessage(
                ":no_entry_sign: **|** Wrong verification code! Please try again **(" + (warnCount + 1) + "/7)**")
            .queue();
      } else {
        event.getChannel().sendMessage(
            ":no_entry_sign: **|** You are banned from using bot for 1 hour\nIf you think it is a mistake, you can ask for help in our [support server](https://discord.gg/RqvTRMmVgR)")
            .queue();
        blockUser(userId);
        captcheMap.remove(userId);
        userWarnsMap.remove(userId);
        removeFile(userId);
        event.getJDA().getChannelById(MessageChannel.class, banLogChannelId)
            .sendMessage("Banned a user with id :" + userId).queue();
      }

    }
  }

  public boolean isUserBanned(long userId) {
    return blockedUsers.contains(userId);
  }

  private void removeFile(long userId) {
    File file = new File("captcha" + userId + ".png");
    file.delete();
  }

  private String createCaptchaText() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      sb.append(charArray[random.nextInt(charArray.length)]);
    }
    return sb.toString();
  }

  private File createImage(String text, String userId) {
    int heigt = 60;
    int width = 220;
    BufferedImage bufferedImage = new BufferedImage(width, heigt, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.setColor(Color.ORANGE);
    createCaptcha(g2d, text);
    g2d.drawLine(5, random.nextInt(50), 220, random.nextInt(50));
    g2d.drawLine(5, random.nextInt(50), 220, random.nextInt(50));

    File imgFile = new File("captcha" + userId + ".png");

    try {
      ImageIO.write(bufferedImage, "png", imgFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imgFile;
  }

  private void createCaptcha(Graphics2D g2d, String captcheText) {
    for (int i = 0; i < captcheText.length(); i++) {
      g2d.setFont(fonts[random.nextInt(this.fonts.length)]);
      g2d.drawString(captcheText.charAt(i) + "", 30 * i + 5, 50);
    }
  }
}
