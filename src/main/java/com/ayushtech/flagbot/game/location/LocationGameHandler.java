package com.ayushtech.flagbot.game.location;

import java.awt.Color;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.PlacesDao;
import com.ayushtech.flagbot.guessGame.place.Place;
import com.ayushtech.flagbot.services.PatreonService;
import com.ayushtech.flagbot.services.VotingService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LocationGameHandler {
  private static LocationGameHandler locationGameHandler = null;

  public static Emoji[] markerEmojis = new Emoji[5];

  static {
    markerEmojis[0] = Emoji.fromCustom("mark1", 1248279606369190029l, false);
    markerEmojis[1] = Emoji.fromCustom("mark2", 1248279612002271372l, false);
    markerEmojis[2] = Emoji.fromCustom("mark3", 1248279617018662922l, false);
    markerEmojis[3] = Emoji.fromCustom("mark4", 1248279620885680209l, false);
    markerEmojis[4] = Emoji.fromCustom("mark5", 1248279628519313559l, false);
  }

  private LocationGameHandler() {
  }

  public static LocationGameHandler getInstance() {
    if (locationGameHandler == null) {
      locationGameHandler = new LocationGameHandler();
    }
    return locationGameHandler;
  }

  public void handleStartGameCommand(SlashCommandInteractionEvent event) {
    long userId = event.getUser().getIdLong();
    if (!VotingService.getInstance().isUserVoted(userId) && !PatreonService.getInstance().isUserPatron(userId)) {
      event.getHook()
          .sendMessage("This command is only for patreon supporters or users who have voted for us in last 24 hours!")
          .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Vote")).queue();
      return;
    }
    LocationMap locationMap = PlacesDao.getInstance().getRandomPlaceMap();
    new LocationGame(event.getHook(), locationMap, userId);
  }

  public void handleStartGameCommand(ButtonInteractionEvent event) {
    long userId = event.getUser().getIdLong();
    if (!VotingService.getInstance().isUserVoted(userId) && !PatreonService.getInstance().isUserPatron(userId)) {
      event.reply("This command is only for patreon supporters or users who have voted for us in last 24 hours!")
          .addActionRow(Button.link("https://top.gg/bot/1129789320165867662/vote", "Vote")).queue();
      return;
    }
    event.deferReply().queue();
    LocationMap locationMap = PlacesDao.getInstance().getRandomPlaceMap();
    new LocationGame(event.getHook(), locationMap, userId);
  }

  public void handleSelection(ButtonInteractionEvent event) {
    long actionUser = event.getUser().getIdLong();
    String[] btnData = event.getComponentId().split("_");
    String registeredUser = btnData[5];
    if (!registeredUser.equals(String.valueOf(actionUser))) {
      event.reply("This button is not for you").setEphemeral(true).queue();
      return;
    }
    String selection = btnData[1];
    String correct = btnData[2];
    String placeCode = btnData[3];
    String mapCode = btnData[3] + "_" + btnData[4];
    String userAvatar = event.getUser().getAvatarUrl();
    String username = event.getUser().getName();
    if (selection.equals(correct)) {
      event
          .editMessageEmbeds(
              getEmbed(mapCode, placeCode, userAvatar, Color.green, username + " selected correct option"))
          .setComponents(getDisabledActionRow(Integer.parseInt(correct)))
          .queue();
      Place place = PlacesDao.getInstance().getPlace(placeCode);
      long balance = CoinDao.getInstance().addCoinsAndGetBalance(actionUser, 100l);
      event.getHook().sendMessageEmbeds(getWinNotifyEmbed(actionUser, correct, place, balance))
          .addActionRow(Button.primary("playAgainLocation", "Play Again")).queue();

    } else {
      event.editMessageEmbeds(getEmbed(mapCode, placeCode, userAvatar, Color.red, username + " selected wrong option"))
          .setComponents(getDisabledActionRow(Integer.parseInt(correct), Integer.parseInt(selection)))
          .queue();
      Place place = PlacesDao.getInstance().getPlace(placeCode);
      event.getHook().sendMessageEmbeds(getLoseNotifyEmbed(actionUser, correct, place))
          .addActionRow(Button.primary("playAgainLocation", "Play Again")).queue();
    }
  }

  public void handleSkipButton(ButtonInteractionEvent event) {
    String actionUserId = event.getUser().getId();
    String[] buttonData = event.getComponentId().split("_");
    String registeredUserId = buttonData[1];
    if (!registeredUserId.equals(actionUserId)) {
      event.reply("You can't use this button.").setEphemeral(true).queue();
      return;
    }
    String placeCode = buttonData[3];
    String mapCode = buttonData[3] + "_" + buttonData[4];
    int correct = Integer.parseInt(buttonData[2]);
    String username = event.getUser().getName();
    String userAvatar = event.getUser().getAvatarUrl();
    event.editMessageEmbeds(getEmbed(mapCode, placeCode, userAvatar, Color.gray, "Skipped by " + username))
        .setComponents(getDisabledActionRow(correct),
            ActionRow.of(Button.primary("playAgainLocation", "Play Again")))
        .queue();
  }

  public void handleViewPlaceButton(ButtonInteractionEvent event) {
    String placeCode = event.getComponentId().split("_")[1];
    EmbedBuilder eb = new EmbedBuilder();
    eb.setImage(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", placeCode));
    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
  }

  private MessageEmbed getEmbed(String mapCode, String placeCode, String userAvatar, Color color,
      String footerMessage) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Select the correct location of the place on the map");
    eb.setColor(color);
    eb.setThumbnail(String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg",
        placeCode));
    eb.setImage(String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places-maps/%s.png",
        mapCode));
    eb.setFooter(footerMessage, userAvatar);
    return eb.build();
  }

  private MessageEmbed getWinNotifyEmbed(long userId, String correctAnswer, Place place, long coins) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Correct!");
    eb.setColor(Color.green);
    eb.setDescription(
        String.format("<@%d> selected the correct option!\n**Coins** : `%d(+100)`<:flag_coin:1472232340523843767>\n**Correct Option** : `%s`",
            userId,
            coins, correctAnswer));
    eb.addField("__Place Information__",
        String.format("**Name :** `%s`\n**Location** : `%s`", place.getName(), place.getLocation()), false);
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", place.getCode()));
    return eb.build();
  }

  private MessageEmbed getLoseNotifyEmbed(long userId, String correctAnswer, Place place) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Wrong!");
    eb.setColor(Color.red);
    eb.setDescription(
        String.format("<@%d> selected the wrong option!\n**Correct Option** : `%s`", userId, correctAnswer));
    eb.addField("__Place Information__",
        String.format("**Name :** `%s`\n**Location** : `%s`", place.getName(), place.getLocation()), false);
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/places/%s.jpg", place.getCode()));
    return eb.build();
  }

  private ActionRow getDisabledActionRow(int correct) {
    ActionRow row = ActionRow.of(
        correct == 1 ? Button.success("1", "1").asDisabled() : Button.primary("1", "1").asDisabled(),
        correct == 2 ? Button.success("2", "2").asDisabled() : Button.primary("2", "2").asDisabled(),
        correct == 3 ? Button.success("3", "3").asDisabled() : Button.primary("3", "3").asDisabled(),
        correct == 4 ? Button.success("4", "4").asDisabled() : Button.primary("4", "4").asDisabled(),
        correct == 5 ? Button.success("5", "5").asDisabled() : Button.primary("5", "5").asDisabled());
    return row;
  }

  private ActionRow getDisabledActionRow(int correct, int wrongSelection) {
    ActionRow row = ActionRow.of(
        correct == 1 ? Button.success("1", "1").asDisabled()
            : wrongSelection == 1 ? Button.danger("1", "1").asDisabled() : Button.primary("1", "1").asDisabled(),
        correct == 2 ? Button.success("2", "2").asDisabled()
            : wrongSelection == 2 ? Button.danger("2", "2").asDisabled() : Button.primary("2", "2").asDisabled(),
        correct == 3 ? Button.success("3", "3").asDisabled()
            : wrongSelection == 3 ? Button.danger("3", "3").asDisabled() : Button.primary("3", "3").asDisabled(),
        correct == 4 ? Button.success("4", "4").asDisabled()
            : wrongSelection == 4 ? Button.danger("4", "4").asDisabled() : Button.primary("4", "4").asDisabled(),
        correct == 5 ? Button.success("5", "5").asDisabled()
            : wrongSelection == 5 ? Button.danger("5", "5").asDisabled() : Button.primary("5", "5").asDisabled());
    return row;
  }

}
