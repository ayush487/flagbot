package com.ayushtech.flagbot.listeners;

import com.ayushtech.flagbot.services.UtilService;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildEventListener extends ListenerAdapter {

  private static String WEBHOOK_URL = "";

  public static void setJoinUpdateWebhookUrl(String url) {
    WEBHOOK_URL = url;
  }

  @Override
  public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
    if (event.getSubcommandName().equals("flag")) {
      if (event.getFocusedOption().getName().equals("mode")) {
        event.replyChoiceStrings("Sovereign Countries Only", "Non-Sovereign Countries Only", "All Countries").queue();
      } else {
        event.replyChoiceStrings("Asia", "Africa", "Europe", "North America", "South America", "Oceania", "Antarctica")
            .queue();
      }
    } else if (event.getSubcommandName().equals("buy") || event.getSubcommandName().equals("sell")) {
      event.replyChoiceStrings("DOOGLE", "MAPPLE", "RAMSUNG", "MICROLOFT", "LOCKSTAR", "SEPSICO", "LETFLIX",
          "STARMUCKS", "TWEETER", "DISKORD").queue();
    } else if (event.getSubcommandName().equals("state_flag")) {
      event
          .replyChoiceStrings("United States", "Brazil", "Germany", "Spain", "Switzerland", "Canada", "Italy", "Russia",
              "Netherlands", "England", "Australia", "Japan")
          .queue();
    } else if (event.getSubcommandName().equals("distance")) {
      event.replyChoiceStrings("Kilometers", "Miles").queue();
    } else if (event.getSubcommandName().equals("set")) {
      event.replyChoiceStrings("Arabic", "Dutch", "French", "German", "Japanese", "Korean", "Portuguese", "Russian",
          "Spanish", "Swedish", "Turkish", "Croatian", "Thai").queue();

    }
  }

  @Override
  public void onGuildJoin(GuildJoinEvent event) {
    UtilService.getInstance().sendMessageToWebhook(WEBHOOK_URL,
        "Flagbot joined server : **" + event.getGuild().getName() + "**");
  }

  @Override
  public void onGuildLeave(GuildLeaveEvent event) {
    UtilService.getInstance().sendMessageToWebhook(WEBHOOK_URL,
        "Flagbot Leaved server : **" + event.getGuild().getName() + "**");
  }
}
