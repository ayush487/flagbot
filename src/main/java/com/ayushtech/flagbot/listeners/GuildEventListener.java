package com.ayushtech.flagbot.listeners;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildEventListener extends ListenerAdapter {

  private final long ownerUserId = 545656364173885440l;

  @Override
  public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
    if (event.getSubcommandName().equals("flag")) {
      event.replyChoiceStrings("Sovereign Countries Only", "Non-Sovereign Countries Only", "All Countries").queue();
    } else if (event.getSubcommandName().equals("buy") || event.getSubcommandName().equals("sell")) {
      event.replyChoiceStrings("DOOGLE", "MAPPLE", "RAMSUNG", "MICROLOFT", "LOCKSTAR", "SEPSICO", "LETFLIX",
          "STARMUCKS", "TWEETER", "DISKORD").queue();
    } else if (event.getSubcommandName().equals("set")) {
      event.replyChoiceStrings("Arabic", "French", "Japanese", "Korean", "Portuguese", "Russian", "Spanish", "Turkish")
          .queue();
    }
  }

  @Override
  public void onGuildJoin(@Nonnull GuildJoinEvent event) {
    event.getJDA().retrieveUserById(ownerUserId).complete().openPrivateChannel().flatMap(
        privateChannel -> privateChannel.sendMessage("Flagbot joined server : **" + event.getGuild().getName() + "**"))
        .queue();
  }

  @Override
  public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
    event.getJDA().retrieveUserById(ownerUserId).complete().openPrivateChannel().flatMap(
        privateChannel -> privateChannel.sendMessage("Flagbot Leaved server : **" + event.getGuild().getName() + "**"))
        .queue();
  }
}
