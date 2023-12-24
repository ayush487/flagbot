package com.ayushtech.flagbot.listeners;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildEventListener extends ListenerAdapter {

  private final long updateChannelId = 1188466978361450506l;

  @Override
  public void onGuildJoin(@Nonnull GuildJoinEvent event) {
    MessageChannel updateChannel = event.getJDA().getChannelById(MessageChannel.class,updateChannelId );
    updateChannel.sendMessage("Flagbot joined server : **" + event.getGuild().getName()+"**").queue();
  }

  @Override
  public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
     MessageChannel updateChannel = event.getJDA().getChannelById(MessageChannel.class,updateChannelId );
     updateChannel.sendMessage("Flagbot Leaved server : **" + event.getGuild().getName()+ "**").queue();
  }
}
