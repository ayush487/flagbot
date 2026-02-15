package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.Optional;

import com.ayushtech.flagbot.dbconnectivity.UserDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class UserService {
  private static UserService instance = null;

	private UserService() {
	}

	public static UserService getInstance() {
		if (instance == null) {
			instance = new UserService();
		}
		return instance;
	}

	public void claimExtraWordCoins(ButtonInteractionEvent event) {
		String buttonOwnerId = event.getComponentId().split("_")[1];
		if (!event.getUser().getId().equals(buttonOwnerId)) {
			event.getHook().sendMessage("This button is not for you!").setEphemeral(true).queue();
			return;
		}
		event.editButton(Button.success("claimed", "Claimed").asDisabled()).queue();
		long userId = event.getUser().getIdLong();
		UserDao.getInstance().claimCoinsWithExtraWords(userId);
		event.getHook().sendMessage("100 <:word_coin:1472270316007981301> added to your balance").setEphemeral(true).queue();
	}

	public void handleDailyCommand(SlashCommandInteractionEvent event) {
		var user = event.getUser();
		Optional<String> optLastDailyDate = UserDao.getInstance().getUserLastDailyDate(user.getIdLong());
		if (optLastDailyDate.isEmpty() || isThisNotToday(optLastDailyDate.get())) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.green);
			eb.setTitle("Daily Rewards");
			// eb.setDescription("100 :coin: is added to your account.");
			eb.setDescription("You received :\n> 1000 <:flag_coin:1472232340523843767>\n> 100 <:word_coin:1472270316007981301>");
			eb.setFooter(user.getName(), user.getAvatarUrl());
			eb.setThumbnail(user.getAvatarUrl());
			event.getHook().sendMessageEmbeds(eb.build())
					// .addActionRow(Button.primary("dailyCrossword", "Play Daily Crossword"))
					.queue();
			UserDao.getInstance().addDailyRewards(user.getIdLong());
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.gray);
			eb.setTitle("Daily Rewards");
			eb.setDescription("You already have claimed daily rewards");
			eb.setFooter(user.getName(), user.getAvatarUrl());
			event.getHook().sendMessageEmbeds(eb.build())
					// disabled this button for now
					// .addActionRow(Button.primary("dailyCrossword", "Play Daily Crossword").asDisabled())
					.queue();
		}
	}

	private boolean isThisNotToday(String lastDailyDate) {
		String currentDate = UtilService.getInstance().getDate();
		return !currentDate.equals(lastDailyDate);
	}

}
