package com.ayushtech.flagbot.services;

import java.awt.Color;
import java.util.Optional;

import com.ayushtech.flagbot.dbconnectivity.CoinDao;
import com.ayushtech.flagbot.dbconnectivity.UserDao;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

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
		event.getHook().sendMessage("100 <:word_coin:1472270316007981301> added to your balance").setEphemeral(true)
				.queue();
	}

	public void handleDailyCommand(SlashCommandInteractionEvent event) {
		var user = event.getUser();
		Optional<String> optLastDailyDate = UserDao.getInstance().getUserLastDailyDate(user.getIdLong());
		if (optLastDailyDate.isEmpty() || isThisNotToday(optLastDailyDate.get())) {
			boolean isUserPatron = PatreonService.getInstance().isUserPatron(user.getIdLong());
			if (isUserPatron) {
				event.getHook().sendMessageEmbeds(getDailyRewardsEmbedPatron(user)).queue();
				CoinDao.getInstance().addDailyRewards(user.getIdLong(), 2000, 200);
			} else {
				event.getHook().sendMessageEmbeds(getDailyRewardsEmbed(user)).queue();
				CoinDao.getInstance().addDailyRewards(user.getIdLong(), 1000, 100);
			}
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.gray);
			eb.setTitle("Daily Rewards");
			eb.setDescription("You already have claimed daily rewards");
			eb.setFooter(user.getName(), user.getAvatarUrl());
			event.getHook().sendMessageEmbeds(eb.build()).queue();
		}
	}

	private MessageEmbed getDailyRewardsEmbedPatron(User user) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Daily Rewards ⭐");
		eb.setColor(Color.getHSBColor(0.7175f, 0.6260f, 0.9647f));
		eb.setDescription(
				"You received **2×** rewards:\n> 2000 <:flag_coin:1472232340523843767>\n> 200 <:word_coin:1472270316007981301>");
		eb.setThumbnail(user.getAvatarUrl());
		eb.setFooter(String.format("Patreon • %s", user.getName()), user.getAvatarUrl());
		return eb.build();
	}

	private MessageEmbed getDailyRewardsEmbed(User user) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.green);
		eb.setTitle("Daily Rewards ⭐");
		eb.setDescription(
				"You received :\n> 1000 <:flag_coin:1472232340523843767>\n> 100 <:word_coin:1472270316007981301>");
		eb.setFooter(user.getName(), user.getAvatarUrl());
		eb.setThumbnail(user.getAvatarUrl());
		return eb.build();
	}

	private boolean isThisNotToday(String lastDailyDate) {
		String currentDate = UtilService.getInstance().getDate();
		return !currentDate.equals(lastDailyDate);
	}

}
