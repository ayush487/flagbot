package com.ayushtech.flagbot.race;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Race {
  private RaceType type;
  private long hostId;
  private Map<Long, Racer> racerMap;
  private boolean isStarted;
  private List<Long> racers;
  private Message message;

  private static String blank = "<:blank:1223533175960109106>";
  private static String finish = "<:finish:1224678758837911614>";
  private static String[] cars = {
      "<:car1:1224656738339524639>",
      "<:car2:1224656733218275328>",
      "<:car3:1224656725848887317>",
      "<:car4:1224656720471654431>",
      "<:car5:1224656714809217045>",
      "<:car6:1224656710489341996>",
      "<:car7:1224656707804991591>"
  };

  public Race(MessageChannelUnion channel, long hostId, String hostName, RaceType type) {
    this.hostId = hostId;
    this.type = type;
    isStarted = false;
    racerMap = new HashMap<Long, Racer>();
    racerMap.put(hostId, new Racer(hostName, cars[0]));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Race");
    eb.setColor(Color.orange);
    eb.setDescription(String.format("__**Type**__ : **%s**", type));
    eb.addField("__Participants__", String.format("<@%d>", hostId), false);
    channel.sendMessageEmbeds(eb.build())
        .setComponents(
            ActionRow.of(Button.success("raceStart", "Start"), Button.primary("raceJoin", "Join Race")),
            ActionRow.of(Button.danger("raceCancel", "Cancel")))
        .queue(message -> setMessage(message));
  }

  public Race startRace(ButtonInteractionEvent event) {
    if (event.getUser().getIdLong() != hostId) {
      event.reply("You cannot start the race!").setEphemeral(true).queue();
      return null;
    }
    if (racerMap.size() == 1) {
      event.reply("You cannot race alone!").setEphemeral(true).queue();
      return null;
    }
    setStarted(true);
    racers = new ArrayList<>(racerMap.keySet());
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Race");
    eb.setColor(Color.orange);
    eb.setDescription(String.format("__**Type**__ : **%s**", type));
    racers.stream()
        .map(r -> racerMap.get(r))
        .forEach(racer -> eb.addField("__" + racer.getName() + "__",
            getGraphicalView(racer.getDistance(), racer.getCar()), false));
    event.editMessageEmbeds(eb.build())
        .setActionRow(Button.primary("accelerate_" + type, Emoji.fromCustom("pedal", 1224246451639681054l, false)))
        .queue();
    return this;
  }

  public boolean endAsCancelled(ButtonInteractionEvent event) {
    if (event.getUser().getIdLong() != hostId) {
      event.reply("You can't use this button").setEphemeral(true).queue();
      return false;
    }
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Race");
    eb.setColor(Color.red);
    eb.setDescription(String.format("__**Type**__ : **%s**", type));
    eb.setFooter("Race canclled!");
    eb.addField("__Participants__", getParticipantsAsString(), false);
    event.editMessageEmbeds(eb.build()).setActionRow(Button.danger("cancelled", "Cancelled").asDisabled())
        .queue();
    return true;
  }

  public void addParticipant(ButtonInteractionEvent event) {
    if (racerMap.containsKey(event.getUser().getIdLong())) {
      event.reply("You already joined the race!").setEphemeral(true).queue();
      return;
    }
    int participantCount = racerMap.size();
    if (participantCount >= 7) {
      event.reply("Maximum 7 participants allowed").queue();
      return;
    }
    racerMap.put(event.getUser().getIdLong(), new Racer(event.getUser().getName(), cars[participantCount]));
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Race");
    eb.setColor(Color.orange);
    eb.setDescription(String.format("__**Type**__ : **%s**", type));
    eb.addField("__Participants__", getParticipantsAsString(), false);
    event.editMessageEmbeds(eb.build()).setComponents(
        ActionRow.of(Button.success("raceStart", "Start"), Button.primary("raceJoin", "Join Race")),
        ActionRow.of(Button.danger("raceCancel", "Cancel")))
        .queue();
    event.getHook().sendMessage("You joined the race").setEphemeral(true).queue();
  }

  public void endAsNotStarted() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.gray);
    eb.setTitle("Race cancelled due to no response");
    eb.setDescription(String.format("__**Type**__ : **%s**", type));
    eb.addField("__Participants__", getParticipantsAsString(), false);
    message.editMessageEmbeds(eb.build()).setActionRow(Button.danger("cancelled", "Timed Out").asDisabled()).queue();
  }

  public void endRaceAsTimeout() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.red);
    eb.setTitle("Race Timed Out");
    eb.setDescription(String.format("__**Type**__ : **%s**", type));
    racers
        .forEach(r -> eb.addField("__" + racerMap.get(r).getName() + "__", racerMap.get(r).getDistance() + "m", false));
    message.editMessageEmbeds(eb.build())
        .setActionRow(
            Button.danger("accelerate_" + type, Emoji.fromCustom("pedal", 1224246451639681054l, false)).asDisabled())
        .queue();
  }

  public boolean moveDistance(long userId, int distance) {
    if (racerMap.containsKey(userId)) {
      EmbedBuilder eb = new EmbedBuilder();
      eb.setDescription(String.format("__**Type**__ : **%s**", type));
      if (racerMap.get(userId).addDistance(distance)) {
        eb.setTitle(String.format("%s won the race!", racerMap.get(userId).getName()));
        eb.setColor(Color.green);
        racers.stream()
            .map(r -> racerMap.get(r))
            .forEach(racer -> eb.addField("__" + racer.getName() + "__",
                getGraphicalView(racer.getDistance(), racer.getCar()), false));
        message.editMessageEmbeds(eb.build())
            .setActionRow(Button.success("accelerate_" + type, Emoji.fromCustom("pedal", 1224246451639681054l, false))
                .asDisabled())
            .queue();
        return true;
      } else {
        eb.setTitle("Race");
        eb.setColor(Color.orange);
        racers.stream()
            .map(r -> racerMap.get(r))
            .forEach(racer -> eb.addField("__" + racer.getName() + "__",
                getGraphicalView(racer.getDistance(), racer.getCar()), false));
        message.editMessageEmbeds(eb.build())
            .setActionRow(Button.primary("accelerate_" + type, Emoji.fromCustom("pedal", 1224246451639681054l, false)))
            .queue();
        return false;
      }
    }
    return false;

  }

  private String getParticipantsAsString() {
    return racerMap.keySet().stream().map(r -> "<@" + r + ">\n").collect(Collectors.joining(" "));
  }

  public long getHostId() {
    return this.hostId;
  }

  public boolean isRacerEntered(long racerId) {
    return racerMap.containsKey(racerId);
  }

  public boolean isStarted() {
    return isStarted;
  }

  public void setStarted(boolean isStarted) {
    this.isStarted = isStarted;
  }

  private void setMessage(Message m) {
    this.message = m;
  }

  private static String getGraphicalView(int score, String car) {
    if (score <= 20) {
      return car + blank + blank + blank + blank + blank + finish;
    } else if (score <= 40) {
      return blank + car + blank + blank + blank + blank + finish;
    } else if (score <= 60) {
      return blank + blank + car + blank + blank + blank + finish;
    } else if (score <= 80) {
      return blank + blank + blank + car + blank + blank + finish;
    } else if (score < 100) {
      return blank + blank + blank + blank + car + blank + finish;
    } else {
      return blank + blank + blank + blank + blank + car + finish;
    }
  }

}