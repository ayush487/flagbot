package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.ayushtech.flagbot.dbconnectivity.LanguageDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LanguageService {
  private static LanguageService languageService = null;
  private Map<Long, String> serverLanguageMapping;
  private Map<String, Map<String, String>> languageMap;

  private LanguageService() {
    serverLanguageMapping = LanguageDao.getInstance().getLanguageMap();
    this.languageMap = LanguageDao.getInstance().getLangMap();
  }

  public static LanguageService getInstance() {
    if (languageService == null) {
      languageService = new LanguageService();
    }
    return languageService;
  }

  public void handleLanguageCommand(SlashCommandInteractionEvent event) {
    String subcommandName = event.getSubcommandName();
    switch (subcommandName) {
      case "info":
        sendLanguageInformation(event);
        return;
      case "set":
        setLanguage(event);
        return;
      case "remove":
        removeLanguage(event);
        return;
      default:
        event.getHook().sendMessage("Something went wrong!").queue();
    }
  }

  public boolean isGuessRight(String language, String guess, String countryCode) {
    if (language == null) {
      return false;
    }
    return languageMap.get(language).get(countryCode).equalsIgnoreCase(guess);
  }

  public Optional<String> getLanguageSelected(long serverId) {
    if (serverLanguageMapping.containsKey(serverId)) {
      return Optional.of(serverLanguageMapping.get(serverId));
    } else {
      return Optional.empty();
    }
  }

  public String getCorrectGuess(String language, String countryCode) {
    return languageMap.get(language).get(countryCode);
  }

  private void removeLanguage(SlashCommandInteractionEvent event) {
    boolean isFromGuild = event.isFromGuild();
    if (!isFromGuild) {
      event.getHook().sendMessage("This command only work in a guild").queue();
      return;
    }
    if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
      long guildId = event.getGuild().getIdLong();
      if (serverLanguageMapping.containsKey(guildId)) {
        serverLanguageMapping.remove(guildId);
        LanguageDao.getInstance().removeGuildLanguage(guildId);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.green);
        eb.setDescription("Removed server language");
        event.getHook().sendMessageEmbeds(eb.build()).queue();
      } else {
        event.getHook().sendMessage("No language selected!").queue(
            m -> m.delete().queueAfter(15, TimeUnit.SECONDS));
      }
    } else {
      event.getHook().sendMessage("You need `MANAGE_SERVER` permissions to remove language.")
          .queue(m -> m.delete().queueAfter(15, TimeUnit.SECONDS));
      return;
    }
  }

  private void setLanguage(SlashCommandInteractionEvent event) {
    boolean isFromGuild = event.isFromGuild();
    if (!isFromGuild) {
      event.getHook().sendMessage("This command only work in a guild").queue();
      return;
    }
    if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
      String language = event.getOption("language").getAsString().toLowerCase();
      if (languageMap.containsKey(language)) {
        long guildId = event.getGuild().getIdLong();
        serverLanguageMapping.put(guildId, language);
        LanguageDao.getInstance().setGuildLanguage(guildId, language);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.green);
        eb.setTitle("Language");
        eb.setDescription("Language selected : `" + language.toUpperCase() + "`");
        event.getHook().sendMessageEmbeds(eb.build()).queue();
      } else {
        event.getHook()
            .sendMessage("The following language : `" + language
                + "` is not supported!\nDo `/language info` to see the supported languages")
            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
      }
    } else {
      event.getHook().sendMessage("You need `MANAGE_SERVER` permissions to change language.")
          .queue(m -> m.delete().queueAfter(15, TimeUnit.SECONDS));
    }
  }

  private void sendLanguageInformation(SlashCommandInteractionEvent event) {
    boolean isFromGuild = event.isFromGuild();
    if (!isFromGuild) {
      event.getHook().sendMessage("This command only work in a guild!").queue();
      return;
    }
    long serverId = event.getGuild().getIdLong();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Languages");
    eb.setColor(Color.YELLOW);
    boolean isLanguageSelected = serverLanguageMapping.containsKey(serverId);
    if (isLanguageSelected) {
      eb.setDescription("__Selected Language__ : `" + serverLanguageMapping.get(serverId).toUpperCase() + "`");
    } else {
      eb.setDescription("No language selected for this server");
    }
    eb.addField("__Supported Languages__",
        "`Arabic`, `French`, `German`, `Japanese`, `Korean`, `Portuguese`, `Russian`, `Spanish`,`Swedish`, `Turkish`", false);
    event.getHook().sendMessageEmbeds(eb.build()).queue();
  }
}