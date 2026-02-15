package com.ayushtech.flagbot.services;

import java.awt.Color;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CoinTransferService {
  private static CoinTransferService coinTransferService = null;

  private CoinTransferService() {
  }

  public static CoinTransferService getInstance() {
    if (coinTransferService == null) {
      coinTransferService = new CoinTransferService();
    }
    return coinTransferService;
  }

  public void handleGiveCoinsCommand(SlashCommandInteractionEvent event) {
    long amount = event.getOption("amount").getAsLong();
    if (amount <= 0) {
      event.getHook().sendMessage("You can't send negative coins.").queue();
      return;
    }
    long receiverId = event.getOption("user").getAsUser().getIdLong();
    long senderId = event.getUser().getIdLong();
    event.getHook().sendMessageEmbeds(sendCoins(senderId, receiverId, amount)).queue();
    return;
  }

  private MessageEmbed sendCoins(long senderId, long receiverId, long amount) {
    EmbedBuilder eb = new EmbedBuilder();
    long senderBalance = CoinDao.getInstance().getBalance(senderId);
    if (senderBalance < amount) {
      eb.setColor(Color.red);
      eb.setTitle("Not enough balance");
      eb.setDescription("You only have `" + senderBalance + "` <:flag_coin:1472232340523843767>");
    } else {
      eb.setColor(Color.green);
      eb.setTitle("Sent successfully!");
      eb.setDescription("You sent `" + amount + "` <:flag_coin:1472232340523843767> to <@" + receiverId + ">");
      CoinDao.getInstance().addCoins(senderId, amount * -1);
      CoinDao.getInstance().addCoins(receiverId, amount);
    }
    return eb.build();
  }
}
