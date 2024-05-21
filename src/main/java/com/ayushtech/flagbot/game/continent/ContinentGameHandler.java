package com.ayushtech.flagbot.game.continent;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ayushtech.flagbot.dbconnectivity.RegionDao;
import com.ayushtech.flagbot.game.Game;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ContinentGameHandler {
  private static ContinentGameHandler handler = null;
  private List<String> countryCodes;
  private Random random;
  private Map<String, String> continentMapping;

  private ContinentGameHandler() {
    random = new Random();
    countryCodes = RegionDao.getInstance().getCountryCodeList();
    loadContinentMap();
  }

  public static ContinentGameHandler getInstance() {
    if (handler == null) {
      handler = new ContinentGameHandler();
    }
    return handler;
  }

  public void handlePlayCommand(SlashCommandInteractionEvent event) {
    OptionMapping roundsOption = event.getOption("rounds");
    int rounds = roundsOption == null ? 0 : roundsOption.getAsInt();
    rounds = (rounds <= 0) ? 0 : (rounds > 15) ? 15 : rounds;
    new ContinentGame(event, getRandomCountryCode());
  }

  public void handlePlayCommand(ButtonInteractionEvent event) {
    new ContinentGame(event, getRandomCountryCode());
  }

  public void handleSelection(ButtonInteractionEvent event) {
    String componentId = event.getComponentId();
    String[] commandData = componentId.split("_");
    String selectedContinent = commandData[1];
    String correctContinent = commandData[3];
    String country = commandData[2];
    if (selectedContinent.equals(correctContinent)) {
      endGameAsWin(event, correctContinent, country);
    } else {
      endGameAsLose(event, correctContinent, selectedContinent,
          country);
    }
  }

  private void endGameAsLose(ButtonInteractionEvent event, String correctContinent,
      String selectedContinent,
      String country) {
    EmbedBuilder eb = new EmbedBuilder();
    User user = event.getUser();
    eb.setTitle("Select which continent this country belongs");
    eb.setColor(Color.red);
    eb.setImage(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/flags/%s.png", country));
    event.editMessageEmbeds(eb.build()).setActionRows(getDisabledActionRowsLose(selectedContinent, correctContinent))
        .queue();
    event.getHook().sendMessageEmbeds(getNotificationEmbedAsLose(user.getName(), country, correctContinent))
        .addActionRow(Button.primary("playAgainContinent", "Play Again")).queue();
  }

  private void endGameAsWin(ButtonInteractionEvent event, String correctContinent, String country) {
    EmbedBuilder eb = new EmbedBuilder();
    User user = event.getUser();
    eb.setTitle("Select which continent this country belongs");
    eb.setColor(Color.green);
    eb.setImage(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/flags/%s.png", country));
    event.editMessageEmbeds(eb.build()).setActionRows(getDisabledActionRowsWin(correctContinent)).queue();
    event.getHook().sendMessageEmbeds(getNotificationEmbedAsWin(user.getIdLong(), country, correctContinent))
        .addActionRow(Button.primary("playAgainContinent", "Play Again"))
        .queue();
    Game.increaseCoins(user.getIdLong(), 100);
  }

  private MessageEmbed getNotificationEmbedAsWin(long userId, String countryCode, String continentCode) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.green);
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/flags/%s.png", countryCode));
    eb.setTitle("Correct!");
    eb.setDescription(
        String.format("<@%d> is correct\n**Coins :** `%d(+100)` :coin:\n**Country :** `%s`\n**Continent :** `%s`", userId,
            Game.getAmount(userId), Game.getCountryName(countryCode),
            continentMapping.get(continentCode)));
    return eb.build();
  }

  private MessageEmbed getNotificationEmbedAsLose(String username, String countryCode, String continentCode) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setColor(Color.red);
    eb.setThumbnail(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/flags/%s.png", countryCode));
    eb.setTitle(String.format("%s selected wrong option!", username));
    eb.setDescription(String.format("**Country :** `%s`\n**Continent :** `%s`", Game.getCountryName(countryCode),
        continentMapping.get(continentCode)));
    return eb.build();
  }

  private ActionRow[] getDisabledActionRowsWin(String continent) {
    ActionRow[] rows = new ActionRow[2];
    rows[0] = ActionRow.of(
        continent.equals("as") ? Button.success("con_as", "Asia").asDisabled()
            : Button.primary("con_as", "Asia").asDisabled(),
        continent.equals("eu") ? Button.success("con_eu", "Europe").asDisabled()
            : Button.primary("con_eu", "Europe").asDisabled(),
        continent.equals("af") ? Button.success("con_af", "Africa").asDisabled()
            : Button.primary("con_af", "Africa").asDisabled(),
        continent.equals("oc") ? Button.success("con_oc", "Oceania").asDisabled()
            : Button.primary("con_oc", "Oceania").asDisabled());
    rows[1] = ActionRow.of(
        continent.equals("na") ? Button.success("con_na", "North America").asDisabled()
            : Button.primary("con_na", "North America").asDisabled(),
        continent.equals("sa") ? Button.success("con_sa", "South America").asDisabled()
            : Button.primary("con_sa", "South America").asDisabled(),
        continent.equals("an") ? Button.success("con_an", "Antarctica").asDisabled()
            : Button.primary("con_an", "Antarctica").asDisabled());
    return rows;
  }

  private ActionRow[] getDisabledActionRowsLose(String selected, String correct) {
    ActionRow[] rows = new ActionRow[2];
    rows[0] = ActionRow.of(
        correct.equals("as") ? Button.success("con_as", "Asia").asDisabled()
            : selected.equals("as") ? Button.danger("con_as", "Asia").asDisabled()
                : Button.primary("con_as", "Asia").asDisabled(),
        correct.equals("eu") ? Button.success("con_eu", "Europe").asDisabled()
            : selected.equals("eu") ? Button.danger("con_eu", "Europe").asDisabled()
                : Button.primary("con_eu", "Europe").asDisabled(),
        correct.equals("af") ? Button.success("con_af", "Africa").asDisabled()
            : selected.equals("af") ? Button.danger("con_af", "Africa").asDisabled()
                : Button.primary("con_af", "Africa").asDisabled(),
        correct.equals("oc") ? Button.success("con_oc", "Oceania").asDisabled()
            : selected.equals("oc") ? Button.danger("con_oc", "Oceania").asDisabled()
                : Button.primary("con_oc", "Oceania").asDisabled());
    rows[1] = ActionRow.of(
        correct.equals("na") ? Button.success("con_na", "North America").asDisabled()
            : selected.equals("na") ? Button.danger("con_na", "North America").asDisabled()
                : Button.primary("con_na", "North America").asDisabled(),
        correct.equals("sa") ? Button.success("con_sa", "South America").asDisabled()
            : selected.equals("sa") ? Button.danger("con_sa", "South America").asDisabled()
                : Button.primary("con_sa", "South America").asDisabled(),
        correct.equals("an") ? Button.success("con_an", "Antarctica").asDisabled()
            : selected.equals("an") ? Button.danger("con_an", "Antarctica").asDisabled()
                : Button.primary("con_an", "Antarctica").asDisabled());
    return rows;
  }

  private String getRandomCountryCode() {
    return countryCodes.get(random.nextInt(countryCodes.size()));
  }

  private void loadContinentMap() {
    continentMapping = new HashMap<>(7);
    continentMapping.put("as", "Asia");
    continentMapping.put("af", "Africa");
    continentMapping.put("an", "Antarctica");
    continentMapping.put("eu", "Europe");
    continentMapping.put("oc", "Oceania");
    continentMapping.put("sa", "South America");
    continentMapping.put("na", "North America");
  }
}
