package com.ayushtech.flagbot.services;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.ayushtech.flagbot.dbconnectivity.ChannelDao;

import net.dv8tion.jda.api.entities.Guild;

public class ChannelService {

  private HashMap<Long,Boolean> disabledMap;
  private static ChannelService channelService = null;

  private ChannelService() {
    disabledMap = new HashMap<Long, Boolean>();
  }

  public static ChannelService getInstance() {
    if(channelService==null) {
      channelService = new ChannelService();
    }
    return channelService;
  }

  public boolean isChannelDisabled(long channelId) {
    if(disabledMap.containsKey(channelId)) {
      return disabledMap.get(channelId);
    }
    boolean isDisabled = ChannelDao.getInstance().isChannelDisabled(channelId);
    disabledMap.put(channelId, isDisabled);
    return isDisabled;
  }

  public synchronized void disableChannel(long channelId) {
    disabledMap.put(channelId, true);
    ChannelDao.getInstance().addDisableChannel(channelId);
  }

  public synchronized void enableChannel(long channelId) {
    disabledMap.put(channelId, false);
    ChannelDao.getInstance().enableChannel(channelId);
  }

  public synchronized void disableMultipleChannels(Guild guild) {
    List<Long> channelIdList = guild.getChannels().stream().map(channel -> channel.getIdLong()).collect(Collectors.toList());
    channelIdList.forEach(channelId -> disabledMap.put(channelId, true));
    ChannelDao.getInstance().addDisableChannel(channelIdList);
  }
}
