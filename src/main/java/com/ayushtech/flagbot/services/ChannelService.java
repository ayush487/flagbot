package com.ayushtech.flagbot.services;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.ayushtech.flagbot.dbconnectivity.ChannelDao;

import net.dv8tion.jda.api.entities.Guild;

public class ChannelService {

  private HashSet<Long> disabledChannels;
  private static ChannelService channelService = null;

  private ChannelService() {
    disabledChannels = new HashSet<>();
  }

  public static synchronized ChannelService getInstance() {
    if(channelService==null) {
      channelService = new ChannelService();
    }
    return channelService;
  }

  public void loadDisabledChannels() {
    List<Long> disabledChannelArray = ChannelDao.getInstance().getAllDisabledChannels();
    for (Long channelId : disabledChannelArray) {
      disabledChannels.add(channelId);
    }
  }

  public boolean isChannelDisabled(long channelId) {
    return disabledChannels.contains(channelId);
  }

  public void disableChannel(long channelId) {
    disabledChannels.add(channelId);
    ChannelDao.getInstance().addDisableChannel(channelId);
  }

  public void enableChannel(long channelId) {
    disabledChannels.remove(channelId);
    ChannelDao.getInstance().enableChannel(channelId);
  }

  public void disableMultipleChannels(Guild guild) {
    List<Long> channelIdList = guild.getChannels().stream().map(channel -> channel.getIdLong()).collect(Collectors.toList());
    ChannelDao.getInstance().addDisableChannel(channelIdList);
    channelIdList.forEach(channelId -> disabledChannels.add(channelId));
  }
}
