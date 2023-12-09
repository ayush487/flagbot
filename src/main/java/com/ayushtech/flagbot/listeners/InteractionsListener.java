package com.ayushtech.flagbot.listeners;

import java.awt.Color;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.ayushtech.flagbot.dbconnectivity.ChannelDao;
import com.ayushtech.flagbot.game.LeaderboardHandler;
import com.ayushtech.flagbot.game.flag.FlagGameEndRunnable;
import com.ayushtech.flagbot.game.flag.FlagGameHandler;
import com.ayushtech.flagbot.game.flag.RegionHandler;
import com.ayushtech.flagbot.game.map.MapGameEndRunnable;
import com.ayushtech.flagbot.game.map.MapGameHandler;
import com.ayushtech.flagbot.services.ChannelService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class InteractionsListener extends ListenerAdapter {

	private ScheduledExecutorService gameEndService;
	private ChannelService channelService;

	public InteractionsListener() {
		super();
		gameEndService = new ScheduledThreadPoolExecutor(4);
		channelService = ChannelService.getInstance();
	}

	@Override
	public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
		event.deferReply().queue();

		// Disable Command
		if (event.getName().equals("disable")) {
			Member member = event.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
				OptionMapping option = event.getOption("channel");
				if (option == null) {
					// channelDao.addDisableChannel(event.getChannel().getIdLong());
					channelService.disableChannel(event.getChannel().getIdLong());
					event.getHook().sendMessage("Commands are disabled for this channel now!").setEphemeral(true).queue();
				} else {
					GuildMessageChannel channelOption = option.getAsMessageChannel();
					if (channelOption == null) {
						event.getHook().sendMessage("Mentioned channel is not a Message Channel").queue();
					} else {
						// channelDao.addDisableChannel(channelOption.getIdLong());
						channelService.disableChannel(channelOption.getIdLong());
						event.getHook().sendMessage("Commands are disabled for " + channelOption.getAsMention() + " now!")
								.setEphemeral(true).queue();
					}
				}
			} else {
				event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
						.queue();
			}
			return;
		}

		// Enable Command
		else if (event.getName().equals("enable")) {
			Member member = event.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
				OptionMapping option = event.getOption("channel");
				if (option == null) {
					channelService.enableChannel(event.getChannel().getIdLong());
					event.getHook().sendMessage("Commands are enabled for this channel now!").setEphemeral(true).queue();
				} else {
					GuildMessageChannel channelOption = option.getAsMessageChannel();
					if (channelOption == null) {
						event.getHook().sendMessage("Mentioned channel is not a Message Channel").queue();
					} else {
						channelService.enableChannel(channelOption.getIdLong());
						event.getHook().sendMessage("Commands are enabled for " + channelOption.getAsMention() + " now!")
								.setEphemeral(true).queue();
					}
				}
			} else {
				event.getHook().sendMessage("You need `Manage_Channel` permissions to use this command!").setEphemeral(true)
						.queue();
			}
		}

		if (channelService.isChannelDisabled(event.getChannel().getIdLong())) {
			event.getHook().sendMessage("Commands are disabled in this channel").setEphemeral(true).queue();
			return;
		}

		if (event.getName().equals("guess")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
		} else if (event.getName().equals("leaderboards")) {
			JDA jda = event.getJDA();
			String temp = LeaderboardHandler.getInstance().getLeaderboard(jda);
			String leaderboard = temp != null ? temp : "Something went wrong!";
			event.getHook().sendMessage(leaderboard).queue();
		} else if (event.getName().equals("guessmap")) {
			boolean isAdded = MapGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(
						new MapGameEndRunnable(
								MapGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
		} else if (event.getName().equals("invite")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setThumbnail("https://cdn.discordapp.com/avatars/1129789320165867662/94a311270ede8ae677711538cc905dd8.png");
			eb.setColor(Color.GREEN);
			eb.setTitle("Invite Flagbot");
			eb.setDescription("Add Flagbot \n[here](https://discord.com/api/oauth2/authorize?client_id=1129789320165867662&permissions=139586824256&scope=applications.commands%20bot)");
			event.getHook().sendMessageEmbeds(eb.build()).queue();
		}
	}

	public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
		super.onButtonInteraction(event);
		if (event.getComponentId().equals("playAgainButton")) {
			boolean isAdded = FlagGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(new FlagGameEndRunnable(
						FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()),
						event.getChannel().getIdLong()), 30, TimeUnit.SECONDS);
			}
		} else if (event.getComponentId().equals("skipButton")) {
			event.deferReply().queue();
			if (FlagGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				FlagGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
		} else if (event.getComponentId().equals("checkRegionButton")) {
			event.deferReply().setEphemeral(true).queue();
			String response = RegionHandler.getInstance().requestForHint(event);
			response = (response != null) ? response : "Region Not Found!";
			event.getHook().sendMessage(response).setEphemeral(true).queue();
			;
		} else if (event.getComponentId().equals("playAgainMap")) {
			boolean isAdded = MapGameHandler.getInstance().addGame(event);
			if (isAdded) {
				gameEndService.schedule(
						new MapGameEndRunnable(
								MapGameHandler.getInstance().getGameMap()
										.get(event.getChannel().getIdLong()),
								event.getChannel().getIdLong()),
						30, TimeUnit.SECONDS);
			}
		} else if (event.getComponentId().equals("skipMap")) {
			event.deferReply().queue();
			if (MapGameHandler.getInstance().getGameMap().containsKey(event.getChannel().getIdLong())) {
				event.getHook().sendMessage(event.getUser().getAsMention() + " has skipped the game!").queue();
				MapGameHandler.getInstance().getGameMap().get(event.getChannel().getIdLong()).endGameAsLose();
			}
		}

	}

}
