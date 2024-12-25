package com.ayushtech.flagbot.game.continent;

import java.awt.Color;

import com.ayushtech.flagbot.guessGame.Country;
import com.ayushtech.flagbot.guessGame.GuessGameUtil;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ContinentGame {
  private String countryCode;
  private String continentCode;
  private Country country;

  public ContinentGame(SlashCommandInteractionEvent event) {
    this.country = GuessGameUtil.getInstance().getRandomCountry();
    this.countryCode = country.getCode();
    this.continentCode = country.getContinentCode();
    // setData();
    event.getHook().sendMessageEmbeds(createEmbed())
    .addComponents(getActionRows()).queue();
  }

  public ContinentGame(ButtonInteractionEvent event) {
    event.deferReply().queue();
    this.country = GuessGameUtil.getInstance().getRandomCountry();
    this.countryCode = country.getCode();
    this.continentCode = country.getContinentCode();
    event.getHook().sendMessageEmbeds(createEmbed()).addComponents(getActionRows()).queue();
  }

  private ActionRow[] getActionRows() {
    ActionRow[] rows = new ActionRow[2];
    rows[0] = ActionRow.of(
      Button.primary(String.format("selectContinent_as_%s_%s",countryCode,continentCode), "Asia"),
      Button.primary(String.format("selectContinent_eu_%s_%s",countryCode,continentCode), "Europe"),
      Button.primary(String.format("selectContinent_af_%s_%s",countryCode,continentCode), "Africa"),
      Button.primary(String.format("selectContinent_oc_%s_%s",countryCode,continentCode), "Oceania")
    );
    rows[1] = ActionRow.of(
      Button.primary(String.format("selectContinent_na_%s_%s",countryCode,continentCode), "North America"),
      Button.primary(String.format("selectContinent_sa_%s_%s",countryCode,continentCode), "South America"),
      Button.primary(String.format("selectContinent_an_%s_%s",countryCode,continentCode), "Antarctica")
    );
    return rows;
  }

  private MessageEmbed createEmbed() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Select which continent this country belongs");
    eb.setColor(Color.cyan);
    eb.setImage(
        String.format("https://raw.githubusercontent.com/ayush487/image-library/main/flags/%s.png", countryCode));
    return eb.build();
  }
}
